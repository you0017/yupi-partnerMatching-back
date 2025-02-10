package com.yupao.job;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupao.model.domain.User;
import com.yupao.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class preCacheJob {

    private final UserService userService;
    private final RedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    //重点用户
    private List<Long> mainUserList = Arrays.asList(1L);
    @Scheduled(cron = "05 11 11 * * ?")
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock("yupao:preCacheJob:doCache:lock");
        try {
            //tryLock会自动读取当前线程信息，释放的时候只能当前线程释放，不指定过期时间会有看门狗机制，自动续过期时间
            if (lock.tryLock(0,TimeUnit.SECONDS)) {
                log.info("getLock:{}",Thread.currentThread().getId());
                for (Long l : mainUserList) {
                    Page<User> userPage = userService.page(new Page<>(1,20));
                    ValueOperations ops = redisTemplate.opsForValue();
                    //如果有缓存，直接拿缓存的
                    String redisKey = String.format("yupao:user:recommand:%s", l);
                    //设置过期时间
                    try {
                        ops.set(redisKey,userPage,10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error("",e);
                    }
                }

                //Thread.sleep(10000000000L);
            }
        } catch (Exception e) {
            log.error("{}",e);
        } finally {
            log.info("unLock:{}",Thread.currentThread().getId());
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}

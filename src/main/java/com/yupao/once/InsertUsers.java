package com.yupao.once;

import com.yupao.mapper.UserMapper;
import com.yupao.model.domain.User;
import com.yupao.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class InsertUsers {
    private final UserMapper userMapper;
    private final UserService userService;

    //@Scheduled(cron = "35 12 16 * * ?")
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final int NUM = 10000;

        List<User> userList = new ArrayList<>();
        User user = null;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            while (true){
                j++;
                user = new User();
                user.setUsername("幽幽");
                user.setUserAccount("youyou");
                user.setAvatarUrl("https://blog.helloyouyou.cn/img/avatar.jpg");
                user.setGender(0);
                user.setUserPassword("b0dd3697a192885d7c055db46155b26a");
                user.setPhone("123");
                user.setEmail("@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setTags("[]");
                user.setProfile("");
                userList.add(user);
                if (j % 10000==0){
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                //看线程id
                System.out.println(Thread.currentThread().getId());
                userService.saveBatch(userList, 10000);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}

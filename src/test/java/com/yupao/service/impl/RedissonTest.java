package com.yupao.service.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class RedissonTest {
    @Autowired
    private RedissonClient redissonClient;
    @Test
    public void test() {
        //list
        List<String> list = new ArrayList<>();
        list.add("1");
        list.get(0);
        list.remove(0);

        RList<String> rList = redissonClient.getList("test-list");
        rList.add("1");
        rList.get(0);
        rList.delete();


        //map
        RMap<Object, Object> map = redissonClient.getMap("test-map");
        map.put("1","2");
        map.delete();

        //set

        //stack
    }
}

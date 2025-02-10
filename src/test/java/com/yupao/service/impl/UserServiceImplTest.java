package com.yupao.service.impl;

import com.yupao.model.domain.User;
import com.yupao.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(20, 20, 1000, java.util.concurrent.TimeUnit.SECONDS, new java.util.concurrent.LinkedBlockingQueue<>(1000));

    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final int NUM = 50000;


        User user = null;
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            List<User> userList = new ArrayList<>();
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
                if (j % NUM==0){
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                //看线程id
                System.out.println(Thread.currentThread().getId());
                userService.saveBatch(userList, 10000);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    public void testMD5() {
        String SALT = "yupi";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+12345678).getBytes());
        System.out.println(encryptPassword);
    }
    @Test
    public void testRegister() {
        String userAccount = "";
        String userPassword = "123456";
        String checkPassword = "123456";
        System.out.println(userService.userRegister(userAccount, userPassword, checkPassword));

        userAccount = "yup";
        userPassword = "123";
        checkPassword = "123456";
        System.out.println(userService.userRegister(userAccount, userPassword, checkPassword));

        userAccount = "yupi";
        userPassword = "123456";
        checkPassword = "123456";
        System.out.println(userService.userRegister(userAccount, userPassword, checkPassword));

        userAccount = "yupi~!@#$%^";
        userPassword = "12345678";
        checkPassword = "12345678";
        System.out.println(userService.userRegister(userAccount, userPassword, checkPassword));

        userAccount = "yupi";
        userPassword = "12345678";
        checkPassword = "12345677";
        System.out.println(userService.userRegister(userAccount, userPassword, checkPassword));

        userAccount = "dogYupi";
        userPassword = "12345678";
        checkPassword = "12345678";
        System.out.println(userService.userRegister(userAccount, userPassword, checkPassword));
    }

    @Test
    void searchUserByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> userList = userService.searchUserByTags(tagNameList);
        userList.forEach(System.out::println);
    }
}
package com.yupao.mapper;

import com.yupao.model.domain.User;
import com.yupao.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

@SpringBootTest
class UserMapperTest {
    @Autowired
    private UserServiceImpl userService;
    @Test
    void testInsert() {
        User user = new User();
        user.setUsername("dogYupi");
        user.setUserAccount("123");
        user.setAvatarUrl("https://baomidou.com/assets/asset.cIbiVTt_.svg");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("qwe");
        user.setUserStatus(0);
        user.setIsDelete(0);

        boolean save = userService.save(user);
        System.out.println(user.getId());
    }

    @Test
    void testMd5(){
        System.out.println(DigestUtils.md5DigestAsHex("12345678".getBytes()));
    }
}
package com.yupao.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedissonConfig {

    private String host;
    private String password;
    private String port;
    private int database;
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                .setPassword(password)
                .setAddress(String.format("redis://%s:%s", host, port));
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}

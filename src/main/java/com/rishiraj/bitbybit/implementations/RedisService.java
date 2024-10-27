package com.rishiraj.bitbybit.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void clearDB(){
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushDb();
    }
}

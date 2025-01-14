package com.rishiraj.bitbybit.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ApiRateLimiter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final int MAX_REQUEST = 5;
    private static final int TIME_WINDOW = 60; // 1 request in 1 minute

    public boolean isAllowed(String userEmail) {
        String key = "rate-limit:" + userEmail;

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();

        Integer currentCount = (Integer) ops.get(key);

        if(currentCount == null){
            // it means the user is accessing the api for the first time
            // or, it means the user is accessing the api after the time window
            //so, we create a new entry for this user
            ops.set(key, 1, TIME_WINDOW, TimeUnit.MINUTES);
            return true;
        }

        if(currentCount < MAX_REQUEST){
            // it means the entry already exists, the user has accessed the api before, but the user has not
            // exceeded the max number of times allowed
            ops.increment(key, 1);
            return true;
        }
        else{
            // user has exceeded the max number of times allowed
            // Block the request if the limit is exceeded
            return false;
        }

    }

}

package com.rishiraj.bitbybit.controllers;

import com.rishiraj.bitbybit.implementations.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    private static final Logger log = LoggerFactory.getLogger(RedisController.class);
    @Autowired
    private RedisService redisService;

    @DeleteMapping("/clear-redis")
    public ResponseEntity<String> clearRedisDB(){
        try{
            log.info("Clear redis called!");
            redisService.clearDB();
            return new ResponseEntity<>("Successful", HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>("Failed to clear redis!",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

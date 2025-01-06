package com.rishiraj.bitbybit.controllers;

import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.VideoResource;
import com.rishiraj.bitbybit.implementations.VideoResourceServiceImpl;
import com.rishiraj.bitbybit.repositories.VideoResourceRepository;
import com.rishiraj.bitbybit.utils.ApiRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/video-resource")
public class VideoResourceController {
    @Autowired
    private ApiRateLimiter apiRateLimiter;
    @Autowired
    private VideoResourceServiceImpl videoResourceService;

    @PostMapping("/create")
    public ResponseEntity<?> createVideoResource(@RequestBody VideoResource videoResource){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String email = authentication.getName();
//
//        if(!apiRateLimiter.isAllowed(email)){
//            return new ResponseEntity<>("Too many requests - try again later", HttpStatus.TOO_MANY_REQUESTS);
//        }

        try{
            VideoResource videoResource1 = videoResourceService.createVideoResource(videoResource);
            return new ResponseEntity<>(Map.of("message", "created Successfully", "data", videoResource1), HttpStatus.OK);
        }
        catch (RuntimeException e){
            return new ResponseEntity<>(Map.of("message", "Video resource already exists", "resourceId", e.getMessage()), HttpStatus.OK);
        }


    }
}

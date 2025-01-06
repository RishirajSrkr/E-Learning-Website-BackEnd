package com.rishiraj.bitbybit.implementations;

import com.rishiraj.bitbybit.entity.VideoResource;
import com.rishiraj.bitbybit.repositories.VideoResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class VideoResourceServiceImpl {

    @Autowired
    private VideoResourceRepository videoResourceRepository;

    public VideoResource createVideoResource(VideoResource videoResource){
        String videoUrl = videoResource.getVideoUrl();
        String s = videoUrlExists(videoUrl);
        if(Objects.equals(s, "NO")){
            return videoResourceRepository.save(videoResource);
        }
        else{
            //throwing the existing resource ID
            throw new RuntimeException(s);
        }
    }

    private String videoUrlExists(String videoUrl) {
        Optional<VideoResource> videoResource = videoResourceRepository.findByVideoUrl(videoUrl);
        if(videoResource.isPresent()){
            return videoResource.get().getId();
        }
        return "NO";
    }
}

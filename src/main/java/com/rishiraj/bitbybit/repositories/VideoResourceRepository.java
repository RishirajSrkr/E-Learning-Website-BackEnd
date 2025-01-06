package com.rishiraj.bitbybit.repositories;

import com.rishiraj.bitbybit.entity.VideoResource;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VideoResourceRepository extends MongoRepository<VideoResource, String> {
    Optional<VideoResource> findByVideoUrl(String videoUrl);
}

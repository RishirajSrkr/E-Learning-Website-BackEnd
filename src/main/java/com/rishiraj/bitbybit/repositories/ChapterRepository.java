package com.rishiraj.bitbybit.repositories;

import com.rishiraj.bitbybit.entity.Chapter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChapterRepository extends MongoRepository<Chapter, ObjectId> {
    List<Chapter> findByCourseId(ObjectId courseId);
    void deleteByCourseId(ObjectId courseId);
}

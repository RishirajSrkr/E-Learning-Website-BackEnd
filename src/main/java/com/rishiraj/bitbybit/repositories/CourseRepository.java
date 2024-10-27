package com.rishiraj.bitbybit.repositories;

import com.rishiraj.bitbybit.entity.Course;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CourseRepository extends MongoRepository<Course, ObjectId> {
    Optional<Course> findByCourseName(String courseName);
}

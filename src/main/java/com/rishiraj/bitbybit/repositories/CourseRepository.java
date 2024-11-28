package com.rishiraj.bitbybit.repositories;

import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CourseRepository extends MongoRepository<Course, ObjectId> {
    Optional<Course> findByCourseName(String courseName);
    Optional<Course> findCreatedBy(ObjectId userId);

}

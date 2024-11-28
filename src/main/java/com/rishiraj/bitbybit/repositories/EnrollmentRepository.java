package com.rishiraj.bitbybit.repositories;

import com.rishiraj.bitbybit.entity.Enrollment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EnrollmentRepository extends MongoRepository<Enrollment, ObjectId> {
    List<Enrollment> findByCourse(ObjectId courseId);
    List<Enrollment> findByUser(ObjectId userId);

}

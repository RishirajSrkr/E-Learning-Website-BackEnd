package com.rishiraj.bitbybit.repositories;

import com.rishiraj.bitbybit.entity.Comment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, ObjectId> {

    List<Comment>findByCourseIdOrderByCreatedAtAsc(ObjectId courseId);
}

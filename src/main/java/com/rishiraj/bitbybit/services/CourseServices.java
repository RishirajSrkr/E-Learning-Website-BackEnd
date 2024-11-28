package com.rishiraj.bitbybit.services;

import com.rishiraj.bitbybit.dto.Course.CourseDto;
import com.rishiraj.bitbybit.entity.Course;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

public interface CourseServices {

    Optional<Course> findCourseById(ObjectId courseId);

    void updateCourse(Course course);

    //get all courses uploaded by a user
    List<Course> getAllCourseUploadedByUser(ObjectId userId) throws Exception;


    //create a new course
    Course createCourse(CourseDto course, MultipartFile courseImage, String email) throws IOException;

    void incrementNumberOfEnrolls(ObjectId courseId) throws Exception;

}

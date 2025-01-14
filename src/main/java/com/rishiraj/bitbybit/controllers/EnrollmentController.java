package com.rishiraj.bitbybit.controllers;

import com.rishiraj.bitbybit.customExceptions.CourseNotFoundException;
import com.rishiraj.bitbybit.customExceptions.UserNotFoundException;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.implementations.EnrollmentService;
import com.rishiraj.bitbybit.repositories.CourseRepository;
import com.rishiraj.bitbybit.repositories.EnrollmentRepository;
import com.rishiraj.bitbybit.repositories.UserRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentController.class);
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CourseRepository courseRepository;

    // ENROLL A USER TO A COURSE
    @PostMapping("/{courseId}")
    public ResponseEntity<String> enrollUser(@PathVariable ObjectId courseId){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email :: " + email));

        try{
            enrollmentService.enrollUserInCourse(user, courseId);
            return new ResponseEntity<>("Successfully Enrolled!", HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    // GET ALL THE USERS WHO HAVE ENROLLED THE COURSE
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<User>> getEnrolledUsers(@PathVariable ObjectId courseId) {
        List<User> enrolledUsers = enrollmentService.getEnrolledUsers(courseId);
        return new ResponseEntity<>(enrolledUsers, HttpStatus.OK);
    }


    // GET ALL THE COURSES A USER HAS ENROLLED TO
    @GetMapping
    public ResponseEntity<?> getEnrolledCourses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email :: " + email));

        log.info("user {} ", user);

        try{
            List<Course> enrolledCourses = enrollmentService.getEnrolledCourses(user.getId());
            Map<String, Course> response = new HashMap<>();
            for(Course course : enrolledCourses){
                response.put(course.getId().toString(), course);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (UserNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // END A COURSE FOR A USER, DE - ENROLL THE USER FROM THE COURSE
    @PostMapping("/end-course/{courseId}")
    public ResponseEntity<?> endCourse(@PathVariable ObjectId courseId) throws AccessDeniedException {

       try{
           //check if the course ID exist
           Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException("Course not found for ID :: " + courseId));

           // only a logged-in user can end his/her enrolled course
           String email = SecurityContextHolder.getContext().getAuthentication().getName();
           User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found for email :: " + email));

           enrollmentService.removeCourseFromEnrollment(user, course);

           return new ResponseEntity<>(HttpStatus.OK);
       }
       catch (CourseNotFoundException | UserNotFoundException e){
           return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

       }
       catch (AccessDeniedException e){
           return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);

       }
       catch (Exception e){
           return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
       }

    }


}

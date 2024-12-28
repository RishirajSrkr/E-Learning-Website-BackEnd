package com.rishiraj.bitbybit.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rishiraj.bitbybit.apiResponses.ApiResponse;
import com.rishiraj.bitbybit.customExceptions.CourseNotFoundException;
import com.rishiraj.bitbybit.customExceptions.UserNotFoundException;
import com.rishiraj.bitbybit.dto.ChapterDto;
import com.rishiraj.bitbybit.dto.Course.CourseDto;
import com.rishiraj.bitbybit.entity.Chapter;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.implementations.CourseServicesImpl;
import com.rishiraj.bitbybit.implementations.VotingServiceImpl;
import com.rishiraj.bitbybit.repositories.UserRepository;
import com.rishiraj.bitbybit.utils.ApiRateLimiter;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/course")
public class CourseControllers {

    private static final Logger log = LoggerFactory.getLogger(CourseControllers.class);

    private final CourseServicesImpl courseServices;
    private final UserRepository userRepository;
    private final VotingServiceImpl votingService;
    private final ApiRateLimiter apiRateLimiter;

    public CourseControllers(CourseServicesImpl courseServices,
                             UserRepository userRepository,
                             ApiRateLimiter apiRateLimiter,
                             VotingServiceImpl votingService
                             ) {
        this.courseServices = courseServices;
        this.userRepository = userRepository;
        this.votingService = votingService;
        this.apiRateLimiter = apiRateLimiter;
    }




    /*
        creating a new course
     */

    @PostMapping("/create")
    public ResponseEntity<?> addCourse(
            @RequestPart("courseDto") String courseDtoJson,
            @RequestPart(value = "file") MultipartFile file,
            @RequestPart(value = "chapters") String chapterJson
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if(!apiRateLimiter.isAllowed(email)){
           return new ResponseEntity<>("Too many requests - try again later", HttpStatus.TOO_MANY_REQUESTS);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Deserialize the JSON string to CourseDto
            CourseDto courseDto = objectMapper.readValue(courseDtoJson, CourseDto.class);

            // Deserialize the JSON string chapters
            List<ChapterDto> chapters = objectMapper.readValue(chapterJson, new TypeReference<List<ChapterDto>>() {
            });
            courseDto.setChapters(chapters);

            Course courseCreated = courseServices.createCourse(courseDto, file, email);
            return new ResponseEntity<>(courseCreated, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            log.error("User not found: ", e);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            log.error("Error uploading image: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Error creating course: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /*
    find a course by its name / title
     */
    @GetMapping("/{title}")
    public Course findCourseByName(@PathVariable String title) {
        SecurityContextHolder.getContext().getAuthentication();
        return courseServices.getCourseByName(title);
    }


    @DeleteMapping("/delete/{courseId}")
    public ResponseEntity<String> deleteCourse(@PathVariable ObjectId courseId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found for email : " + email));

        try {
            courseServices.deleteCourse(courseId, user);
            return new ResponseEntity<>("Course deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete course with ID : " + courseId, HttpStatus.BAD_REQUEST);
        }
    }


    /*
    GET UPLOADED COURSES
     */
    @GetMapping("/uploaded-courses")
    public ResponseEntity<Map<String, CourseDto>> getUploadedCourses() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email : " + email + " not found"));
            List<Course> allCourseUploadedByUser = courseServices.getAllCourseUploadedByUser(user.getId());

            Map<String, CourseDto> response = new HashMap<>();
            for (Course course : allCourseUploadedByUser) {
                CourseDto courseDto = courseServices.convertToCourseDto(course);
                response.put(course.getId().toString(), courseDto);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Collections.emptyMap(), HttpStatus.OK);
        }
    }


    /*
    VOTE A COURSE
     */

    @PostMapping("/{courseId}/vote-course")
    public ResponseEntity<String> voteCourse(@PathVariable ObjectId courseId) {
        try {
            votingService.voteCourse(courseId);
            return new ResponseEntity<>("Vote Successful!", HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




}

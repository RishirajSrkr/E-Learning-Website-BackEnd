package com.rishiraj.bitbybit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rishiraj.bitbybit.customExceptions.CourseNotFoundException;
import com.rishiraj.bitbybit.customExceptions.UserCreationException;
import com.rishiraj.bitbybit.customExceptions.UserNotFoundException;
import com.rishiraj.bitbybit.dto.RegisterUserDto;
import com.rishiraj.bitbybit.dto.User.UserDto;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.implementations.CourseServicesImpl;
import com.rishiraj.bitbybit.implementations.UserDetailServiceImpl;
import com.rishiraj.bitbybit.implementations.UserServicesImpl;
import com.rishiraj.bitbybit.repositories.CourseRepository;
import com.rishiraj.bitbybit.repositories.UserRepository;
import com.rishiraj.bitbybit.utils.ApiRateLimiter;
import com.rishiraj.bitbybit.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/public")
public class PublicControllers {

    private static final Logger log = LoggerFactory.getLogger(PublicControllers.class);
    private final UserDetailServiceImpl userDetailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserServicesImpl userServices;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseServicesImpl courseServices;


    public PublicControllers(UserServicesImpl userServices,
                             JwtUtils jwtUtils,
                             AuthenticationManager authenticationManager,
                             UserDetailServiceImpl userDetailService,
                             CourseRepository courseRepository,
                             UserRepository userRepository,
                             CourseServicesImpl courseServices) {
        this.userServices = userServices;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userDetailService = userDetailService;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseServices = courseServices;
    }


    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("Health Check :: OK", HttpStatus.OK);
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid
            @RequestPart("registerUserDto") String registerUserDtoJson,
            @RequestPart(value = "file") MultipartFile userProfileImage
    ) throws Exception {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            RegisterUserDto registerUserDto = objectMapper.readValue(registerUserDtoJson, RegisterUserDto.class);
            User createdUser = userServices.createUser(registerUserDto, userProfileImage);

            log.info("----------------------------------------");
            log.info("Created User :: {} ", createdUser);
            log.info("----------------------------------------");


            return new ResponseEntity<>(createdUser, HttpStatus.OK);
        } catch (
                UserCreationException e) {
            return new ResponseEntity<>("Email already exists.", HttpStatus.BAD_REQUEST);
        } catch (
                Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
        try {
            Authentication authenticate = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
            log.info("----------------------------------------");
            log.info("authenticate {} ", authenticate);
            log.info("----------------------------------------");
            String jwt = jwtUtils.generateToken(user.getEmail());

            /*
            creating a response map to send in response body.
             */

            Map<String, Object> responseToSend = new HashMap<>();
            responseToSend.put("jwtToken", jwt);

            log.info("----------------------------------------");
            log.info(jwt);
            log.info("----------------------------------------");

            return new ResponseEntity<>(responseToSend, HttpStatus.OK);
        } catch (
                BadCredentialsException e) {
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        } catch (
                Exception e) {
            return new ResponseEntity<>("An error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    /*
    get all the courses, this is public since we want a user to be able to see all courses without even login or registration
     */
    @GetMapping("/all-courses")
    public ResponseEntity<?> getAllCourses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Map<String, Course> responseToSend = new HashMap<>();

        //if no user is logged in, return all the courses
        if (email.equals("anonymousUser") || email.isEmpty()) {
            List<Course> allCourses = courseRepository.findAll();
            for (Course course : allCourses) {
                responseToSend.put(course.getId().toString(), course);
            }
            return new ResponseEntity<>(responseToSend, HttpStatus.OK);
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));

        try {
            List<Course> coursesNotUploadedByUser = courseServices.getAllCourses(user);

            for (Course course : coursesNotUploadedByUser) {
                responseToSend.put(course.getId().toString(), course);
            }

            return new ResponseEntity<>(responseToSend, HttpStatus.OK);
        } catch (
                Exception e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    /*
    get a course by id
     */
    @GetMapping("/course/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable ObjectId id) {
        Optional<Course> optionalCourse = courseRepository.findById(id);
        if (optionalCourse.isPresent()) {
            return new ResponseEntity<>(optionalCourse.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /*
    get all the contributors / users
    every user can see the list of contributors without logging or registration
     */
    @GetMapping("/contributors")
    public ResponseEntity<?> getAllUser() {
        try {
            List<UserDto> allContributors = userServices.getAllContributor();

            Map<String, UserDto> response = new HashMap<>();
            for (UserDto user : allContributors) {
                response.put(user.getUserId().toString(), user);
            }

            log.info("response {} ", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (
                Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/incrementEnrolls")
    public ResponseEntity<String> incrementCourseEnrolls(@PathVariable ObjectId courseId) {

        try {
            courseServices.incrementNumberOfEnrolls(courseId);
            return new ResponseEntity<>("Incremented Successfully.", HttpStatus.OK);
        } catch (
                CourseNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (
                Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /*
   GET TOP VOTED COURSE -- TOP 3
    */
    @GetMapping("/top-voted")
    public ResponseEntity<List<Course>> getTopVotedCourses() {
        try {
            List<Course> topVotedCourses = courseServices.getTopVotedCourses();
            return new ResponseEntity<>(topVotedCourses, HttpStatus.OK);
        } catch (
                Exception e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

}

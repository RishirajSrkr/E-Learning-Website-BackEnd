package com.rishiraj.bitbybit.controllers;

import com.rishiraj.bitbybit.customExceptions.CourseNotFoundException;
import com.rishiraj.bitbybit.customExceptions.UserCreationException;
import com.rishiraj.bitbybit.dto.UserDto;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.implementations.CourseServicesImpl;
import com.rishiraj.bitbybit.implementations.UserDetailServiceImpl;
import com.rishiraj.bitbybit.implementations.UserServicesImpl;
import com.rishiraj.bitbybit.repositories.CourseRepository;
import com.rishiraj.bitbybit.repositories.UserRepository;
import com.rishiraj.bitbybit.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
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
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseServicesImpl courseServices;



    public PublicControllers(UserServicesImpl userServices, JwtUtils jwtUtils, AuthenticationManager authenticationManager, UserDetailServiceImpl userDetailService, CourseRepository courseRepository) {
        this.userServices = userServices;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userDetailService = userDetailService;
        this.courseRepository = courseRepository;
    }


    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        return new ResponseEntity<>("Health Check :: OK", HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) throws Exception {
       try{
           User creaedUser = userServices.createUser(user);
           return new ResponseEntity<>(creaedUser, HttpStatus.OK);
       }
       catch (UserCreationException e){
           return new ResponseEntity<>("Email already exists.", HttpStatus.BAD_REQUEST);
       }
       catch (Exception e){
           return new ResponseEntity<>("An error occurred during registration.", HttpStatus.INTERNAL_SERVER_ERROR);
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
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromRequest(request);
        log.info("INSIDE LOGOUT ::  {} ", jwt);

        if (jwt != null) {
            try {
                jwtUtils.blacklistToken(jwt, jwtUtils.convertDateToLocalDateTime(jwt));
                return new ResponseEntity<>("Logout successful", HttpStatus.OK);
            } catch (ExpiredJwtException e) {
                log.warn("JWT expired. User is logged out");
                return new ResponseEntity<>("Token has expired. You are logged out.", HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                log.error("Error during logout: {}", e.getMessage());
                return new ResponseEntity<>("Logout failed", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("No token found", HttpStatus.BAD_REQUEST);
        }
    }

    /*
    get all the courses, this is public since we want a user to be able to see all courses without even login or registration
     */
    @GetMapping("/all-courses")
    public ResponseEntity<?> getAllCourses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("email is :: {} ", email);
        try {
            /*
            we have filtered out the courses uploaded and enrolled by the user in service layer
             */
            List<Course> coursesNotUploadedByUser = courseServices.getAllCourses(email);


             /*
            I was having problem in accessing course id in frontend so sending course id in a more descriptive way here as string
             */
            Map<String, Course> responseToSend = new HashMap<>();
            for (Course course : coursesNotUploadedByUser) {
                responseToSend.put(course.getId().toString(), course);
            }

            return new ResponseEntity<>(responseToSend, HttpStatus.OK);

        } catch (Exception e) {
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
            for(UserDto user : allContributors){
                response.put(user.getUserId().toString(), user);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/incrementEnrolls")
    public ResponseEntity<String> incrementCourseEnrolls(@PathVariable ObjectId courseId){

        try{
            courseServices.incrementNumberOfEnrolls(courseId);
            return new ResponseEntity<>("Incremented Successfully.", HttpStatus.OK);
        }
        catch (CourseNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


     /*
    GET TOP VOTED COURSE -- TOP 3
     */
    @GetMapping("/top-voted")
    public ResponseEntity<List<Course>> getTopVotedCourses(){
        try{
            List<Course> topVotedCourses = courseServices.getTopVotedCourses();
            return new ResponseEntity<>(topVotedCourses, HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

}

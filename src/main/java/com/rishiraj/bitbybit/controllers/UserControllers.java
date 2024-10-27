package com.rishiraj.bitbybit.controllers;

import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.implementations.UserDetailServiceImpl;
import com.rishiraj.bitbybit.implementations.UserServicesImpl;
import com.rishiraj.bitbybit.repositories.UserRepository;
import com.rishiraj.bitbybit.utils.JwtUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.HTML;
import java.util.*;


@RestController
@RequestMapping("/user")
public class UserControllers {

    private static final Logger log = LoggerFactory.getLogger(UserControllers.class);

    private final UserServicesImpl userServices;
    private final UserRepository userRepository;

    public UserControllers(UserServicesImpl userServices,
                           UserRepository userRepository
    ) {
        this.userServices = userServices;
        this.userRepository = userRepository;
    }


    /* create a new user */
    @PostMapping("/register")
    public ResponseEntity<User> createUser(@RequestBody User user) throws Exception {
        log.info("user {} ", user);
        User newUser = userServices.createUser(user);
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    /* get user by user ID */
    @GetMapping("/id/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable ObjectId userId) {
        Optional<User> user = userServices.getUserById(userId);
        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.FOUND);
        } else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /* only a logged-in user can delete their account
    so, we can check who is logged in and delete the user
    */
    @DeleteMapping()
    public ResponseEntity<String> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); //this line returns the email in our case, because we used email in userDetailServiceImpl too.
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            userRepository.deleteById(user.get().getId());
            return new ResponseEntity<>("Deleted Successfully.", HttpStatus.OK);
        } else return new ResponseEntity<>("Failed to delete.", HttpStatus.NOT_FOUND);

    }

    /*
    anyone can access this
     */
    @GetMapping("/userId/{userId}/uploaded-courses")
    public ResponseEntity<?> getCoursesUploadedByUser(@PathVariable ObjectId userId) {
        try{
            log.info("user id ----- {} ", userId);
            List<Course> allCoursesUploadedByUser = userServices.getAllCoursesUploadedByUser(userId);
            if(allCoursesUploadedByUser.isEmpty()){
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
            }
            return new ResponseEntity<>(allCoursesUploadedByUser, HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>("Exception while trying to fetch user uploaded courses.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /*
    this can only be accessed by the logged-in user, which means a user can see only his enrolled courses.
    so we don't need to pass user Id, we can take the user info out from authentication object
     */
    @GetMapping("/enrolled-courses")
    public ResponseEntity<List<Course>> getCoursesEnrolledByUser() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        List<Course> allCoursesUploadedByUser = userServices.getAllCoursesEnrolledByUser(email);
        return new ResponseEntity<>(allCoursesUploadedByUser, HttpStatus.OK);
    }


}

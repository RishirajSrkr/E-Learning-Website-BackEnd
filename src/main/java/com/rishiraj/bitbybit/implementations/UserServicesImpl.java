package com.rishiraj.bitbybit.implementations;

import com.rishiraj.bitbybit.customExceptions.UserCreationException;
import com.rishiraj.bitbybit.dto.UserDto;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.repositories.UserRepository;
import com.rishiraj.bitbybit.services.UserService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServicesImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServicesImpl.class);
    private final UserRepository userRepository;
    private final CourseServicesImpl courseServices;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public UserServicesImpl(UserRepository userRepository, CourseServicesImpl courseServices) {
        this.userRepository = userRepository;
        this.courseServices = courseServices;
    }




    // this is for creating a user
    public User createUser(User user) throws Exception {
        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        if(optionalUser.isEmpty()){
            try{
                //hashing the password
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                //set default role
                user.getRoles().add("USER");
                userRepository.save(user);
                return user;
            }
            catch (Exception e){
                throw new Exception();
            }
        }
        else{
            //it means a user with the email already exists
            throw new UserCreationException("Email already exists");
        }
    }

    //for updating a user
    public void updateUser(User user) throws UsernameNotFoundException {
        try {
            userRepository.save(user);
        } catch (Exception e) {
            log.error("error while updating user: ", e);
        }
    }


    public Optional<User> getUserById(ObjectId userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deleteUserById(ObjectId userId) {
        userRepository.deleteById(userId);
    }

    //get all the courses that are uploaded by the user
    public List<Course> getAllCoursesUploadedByUser(ObjectId userId) throws Exception {
        return courseServices.getAllCourseUploadedByUser(userId);
    }

    //get all the courses that are enrolled by the user
    public List<Course> getAllCoursesEnrolledByUser(String email) throws Exception {
        return courseServices.getAllCoursesEnrolledByUser(email);
    }


    /*
    get all users / contributors
     */
    public List<UserDto> getAllContributor() {
        /*
        getting those users who have at least uploaded one course / content
         */
        List<User> usersWithUploads = userRepository.findAll().stream().filter(user -> user.getUploadedCourse().size() >= 1).collect(Collectors.toList());

        /*
        converting the above data into our need, we don't want to return all the fields of actual user class, so we use a DTO
         */
        return usersWithUploads.stream().map(user -> UserDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bio(user.getBio())
                .uploadedCourse(user.getUploadedCourse().size())
                .build()).collect(Collectors.toList());

    }

}

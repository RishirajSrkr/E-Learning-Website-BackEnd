package com.rishiraj.bitbybit.implementations;

import com.rishiraj.bitbybit.customExceptions.UserCreationException;
import com.rishiraj.bitbybit.dto.RegisterUserDto;
import com.rishiraj.bitbybit.dto.User.UserDto;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.repositories.UserRepository;
import com.rishiraj.bitbybit.services.UserService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServicesImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServicesImpl.class);
    private final UserRepository userRepository;
    private final CourseServicesImpl courseServices;
    private final CloudinaryImageUploadServiceImpl imageUploadService;


    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public UserServicesImpl(UserRepository userRepository, CourseServicesImpl courseServices, CloudinaryImageUploadServiceImpl imageUploadService) {
        this.userRepository = userRepository;
        this.courseServices = courseServices;
        this.imageUploadService = imageUploadService;
    }


    // this is for creating a user
    public User createUser(RegisterUserDto registerUserDto, MultipartFile profileImage) throws Exception {

        Optional<User> optionalUser = userRepository.findByEmail(registerUserDto.getEmail());

        if (optionalUser.isPresent()) {
            throw new UserCreationException("User already exists");
        }

        try {
            //creating a user object from registerUserDto

            User newUser = User.builder()
                    .name(registerUserDto.getName())
                    .email(registerUserDto.getEmail())
                    .bio(registerUserDto.getBio())
                    .password(passwordEncoder.encode(registerUserDto.getPassword()))
                    .roles(Set.of("USER"))
                    .build();


            //upload profile image to cloudinary and get the URL
            String imageUrl = null;
            if (profileImage != null && !profileImage.isEmpty()) {
                Map uploadResult = imageUploadService.uploadFile(profileImage);
                imageUrl = (String) uploadResult.get("url");


            }

            newUser.setProfileImageUrl(imageUrl);
            userRepository.save(newUser);

            log.info("saved user {}", newUser);

            return newUser;

        } catch (Exception e) {
            throw new Exception();
        }


    }


    //update a user details
    public void updateUserData(User user, RegisterUserDto registerUserDto, MultipartFile image) throws IOException {

        user.setName(registerUserDto.getName() != null && !registerUserDto.getName().isEmpty() ? registerUserDto.getName() : user.getName());
        user.setEmail(registerUserDto.getEmail() != null && !registerUserDto.getEmail().isEmpty() ? registerUserDto.getEmail() : user.getEmail());
        user.setBio(registerUserDto.getBio() != null && !registerUserDto.getBio().isEmpty() ? registerUserDto.getBio() : user.getBio());

        //hash the password before saving
        if (registerUserDto.getPassword() != null && !registerUserDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        }

        String imageUrl = "";
        if (image == null || image.isEmpty()) {
            //it means user has not updated the profile image
            imageUrl = user.getProfileImageUrl();
        } else {
            Map uploadResult = imageUploadService.uploadFile(image);
            imageUrl = (String) uploadResult.get("url");
        }
        user.setProfileImageUrl(imageUrl);
        log.info("new image :: {} ", imageUrl);

        User savedUser = userRepository.save(user);
        log.info("saved user {}", savedUser);

    }


    //get all the courses that are uploaded by the user
    public List<Course> getAllCoursesUploadedByUser(ObjectId userId) throws Exception {
        return courseServices.getAllCourseUploadedByUser(userId);
    }

    /*
    get all users / contributors
     */
    public List<UserDto> getAllContributor() {
        /*
        getting those users who have at least uploaded one course / content
         */
        List<User> usersWithUploads = userRepository.findAll().stream().filter(user -> user.getUploadedCourses().size() >= 1).collect(Collectors.toList());

        /*
        converting the above data into our need, we don't want to return all the fields of actual user class, so we use a DTO
         */
        return usersWithUploads.stream().map(user -> UserDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bio(user.getBio())
                .uploadedCourses(user.getUploadedCourses().size())
                .profileImage(user.getProfileImageUrl())
                .build()).collect(Collectors.toList());

    }


    /*
    total number of votes a user has got
     */
    public Integer totalVote(User user) {
        return user.getUploadedCourses().stream()
                .mapToInt(course -> course.getVotes())
                .sum();
    }
}

package com.rishiraj.bitbybit.services;

import com.rishiraj.bitbybit.dto.Course.CourseDto;
import com.rishiraj.bitbybit.dto.RegisterUserDto;
import com.rishiraj.bitbybit.dto.User.UserDto;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;


public interface UserService {
    void updateUserData (User user, RegisterUserDto registerUserDto, MultipartFile file) throws Exception;

    Optional<User> getUserById(ObjectId userId);

    Optional<User> getUserByEmail(String email);

    void deleteUserById(ObjectId userId);


    User createUser(RegisterUserDto registerUserDto, MultipartFile profileImage) throws Exception;

    List<UserDto> getAllContributor();
}

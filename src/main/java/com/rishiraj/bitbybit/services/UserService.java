package com.rishiraj.bitbybit.services;

import com.rishiraj.bitbybit.customExceptions.UserCreationException;
import com.rishiraj.bitbybit.dto.RegisterUserDto;
import com.rishiraj.bitbybit.dto.UserDto;
import com.rishiraj.bitbybit.dto.UserUpdateDto;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;


public interface UserService {
    void updateUserData (User user, UserUpdateDto userUpdateDto) throws Exception;

    Optional<User> getUserById(ObjectId userId);

    Optional<User> getUserByEmail(String email);

    void deleteUserById(ObjectId userId);

    List<Course> getAllCoursesUploadedByUser(ObjectId userId) throws Exception;

    List<Course> getAllCoursesEnrolledByUser(String email) throws Exception;

    User createUser(RegisterUserDto registerUserDto, MultipartFile profileImage) throws Exception;

    List<UserDto> getAllContributor();
}

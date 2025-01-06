package com.rishiraj.bitbybit.services;

import com.rishiraj.bitbybit.dto.RegisterUserDto;
import com.rishiraj.bitbybit.dto.User.UserDto;
import com.rishiraj.bitbybit.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface UserService {
    void updateUserData (User user, RegisterUserDto registerUserDto, MultipartFile file) throws Exception;

    User createUser(RegisterUserDto registerUserDto, MultipartFile profileImage) throws Exception;

    List<UserDto> getAllContributor();
}

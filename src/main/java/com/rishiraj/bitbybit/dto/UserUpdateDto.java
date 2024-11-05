package com.rishiraj.bitbybit.dto;

import lombok.Data;

@Data
public class UserUpdateDto {
    private String name;
    private String email;
    private String password;
    private String bio;
    private String ProfileImage;
}

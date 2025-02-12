package com.rishiraj.bitbybit.dto;

import lombok.Data;

@Data
public class RegisterUserDto {
    private String name;
    private String email;
    private String bio;
    private String password;
    private String role = "USER";
}

package com.rishiraj.bitbybit.dto;

/*
this dto is needed for 'show all user' in frontend
I don't want to send all fields of actual User class in response.
 */

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class UserDto {
    private ObjectId userId;
    private String name;
    private String email;
    private String bio;
    private int uploadedCourse;
    private String profileImage;
}

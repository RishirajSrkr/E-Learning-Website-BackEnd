package com.rishiraj.bitbybit.dto.User;

/*
this dto is needed for 'show all user' in frontend
I don't want to send all fields of actual User class in response.
 */

import com.rishiraj.bitbybit.entity.Course;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@Builder
public class UserDto {
    private ObjectId userId;
    private String name;
    private String email;
    private String bio;
    private int uploadedCourses;
    private List<String> enrolledCourses;
    private String profileImage;
}

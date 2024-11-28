package com.rishiraj.bitbybit.dto.User;

/*
this dto is needed for 'show all user' in frontend
I don't want to send all fields of actual User class in response.
 */

import com.rishiraj.bitbybit.dto.Course.CourseDto;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@Builder
public class UserWithCoursesDto {
    private ObjectId userId;
    private String name;
    private String email;
    private String bio;
    private String profileImage;
    private List<CourseDto> uploadedCourses;
}

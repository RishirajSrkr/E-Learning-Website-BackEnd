package com.rishiraj.bitbybit.dto.Course;

import com.rishiraj.bitbybit.dto.User.UserDto;
import com.rishiraj.bitbybit.entity.Chapter;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

/*

In a DTO (Data Transfer Object), you should only include the
fields that are relevant to the data being sent from the frontend
to the backend. This means that your DTO should represent the
structure of the incoming data and only include the properties
that the frontend sends.

 */
@Data
@Builder
public class CourseDtoWithEnrolledUsers {
    private ObjectId courseId;
    private String courseName;
    private String courseDescription;
    private String courseCategory;
    private List<Chapter> chapters;
    private int numberOfEnrolls;
    private LocalDateTime createdAt;
    private ObjectId createdBy;
    private String courseImage;
    private int votes;
    private List<UserDto> enrolledBy;
}

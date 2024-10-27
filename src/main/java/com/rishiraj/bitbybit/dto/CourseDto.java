package com.rishiraj.bitbybit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rishiraj.bitbybit.entity.Chapter;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*

In a DTO (Data Transfer Object), you should only include the
fields that are relevant to the data being sent from the frontend
to the backend. This means that your DTO should represent the
structure of the incoming data and only include the properties
that the frontend sends.

 */
@Data
public class CourseDto {


    private String courseName;

    private String courseDescription;
    private String courseCategory;
    private List<ChapterDto> chapters;
}

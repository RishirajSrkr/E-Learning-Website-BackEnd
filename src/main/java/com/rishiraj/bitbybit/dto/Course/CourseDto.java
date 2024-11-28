package com.rishiraj.bitbybit.dto.Course;

import com.rishiraj.bitbybit.dto.ChapterDto;
import com.rishiraj.bitbybit.entity.Chapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

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
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private String courseName;
    private String courseDescription;
    private String courseCategory;
    private List<ChapterDto> chapters;
}

package com.rishiraj.bitbybit.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Document(collection = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course implements Comparable<Course> {
    @Id
    @Indexed
    private ObjectId id;

    @NotBlank
    @Size(max = 100)
    @Indexed
    private String courseName;

    @NotBlank
    @Size(max = 1000)
    private String courseDescription;

    private String instructorName;

    @NotBlank
    private String courseCategory;

    private ObjectId createdBy;

    @Indexed(name = "votes_index", direction = IndexDirection.DESCENDING)
    private int votes;

    private int numberOfEnrolls;

    private LocalDateTime createdAt;

    @DBRef
    private List<Chapter> chapters = new ArrayList<>();

    @NotBlank
    private String imageUrl;



    @Override
    public int compareTo(Course o) {
        if(this.getVotes() > o.getVotes()) return -1;
        else if(this.getVotes() < o.getVotes()) return 1;
        else return 0;
    }
}

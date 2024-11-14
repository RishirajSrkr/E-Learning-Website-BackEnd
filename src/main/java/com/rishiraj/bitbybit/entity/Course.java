package com.rishiraj.bitbybit.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
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
    private ObjectId id;
    @NonNull
    private String courseName;
    @NonNull
    private String courseDescription;

    /*
    the client doesn't have to add this in the course form, we will add this on the server side based on the currently logged-in user
     */
    private String instructorName;

    @NonNull
    private String courseCategory;

    //id of user who created the course
    private ObjectId createdBy;

    private int vote;

    private int numberOfEnrolls;

    @DBRef
    private List<User> enrolledBy;

    private LocalDateTime createdAt;

    // to store the list of chapters of this course
    @DBRef
    private List<Chapter> chapters = new ArrayList<>();


    // for storing course thumbnail image
    private String imageUrl;



    @Override
    public int compareTo(Course o) {
        if(this.getVote() > o.getVote()) return -1;
        else if(this.getVote() < o.getVote()) return 1;
        else return 0;
    }
}

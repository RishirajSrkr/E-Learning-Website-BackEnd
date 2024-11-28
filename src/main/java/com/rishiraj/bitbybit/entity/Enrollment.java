package com.rishiraj.bitbybit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "enrollments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    @Id
    private ObjectId id;
    @DBRef
    private Course course;
    @DBRef
    private User user;
    private LocalDateTime enrolledAt;
}

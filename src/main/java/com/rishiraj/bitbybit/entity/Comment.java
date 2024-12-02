package com.rishiraj.bitbybit.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment {
    @Id
    private ObjectId id;

    @NotNull
    private ObjectId courseId;

    private ObjectId userId;

    @NotNull
    @Size(min = 1, max = 500)
    private String content;


    @Indexed
    private LocalDateTime createdAt;

    private List<ObjectId> replies;
}

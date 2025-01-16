package com.rishiraj.bitbybit.entity;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chapters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {
    @Id
    private ObjectId id;
    @NotNull
    private String chapterName;
    @NotNull
    private String chapterContent;
    private String videoLink;
    private LocalDateTime createdAt;
    private ObjectId courseId;
}

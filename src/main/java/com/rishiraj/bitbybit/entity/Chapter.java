package com.rishiraj.bitbybit.entity;

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
    @NonNull
    private String chapterName;
    @NonNull
    private String chapterContent;
    private LocalDateTime createdAt;

    /*
    in which course does this chapter belongs, this is needed to delete the chapter when the course is deleted,
    because in Mongo db we don't have on delete cascade
     */
    private ObjectId courseId;
}

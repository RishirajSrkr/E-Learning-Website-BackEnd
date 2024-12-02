package com.rishiraj.bitbybit.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reply {
    @Id
    private ObjectId id;

    //to which comment this reply belongs to
    @NotNull
    private ObjectId commentId;

    //there can be nested replies so this will be null for top-level replies
    private ObjectId parentReplyId;

    @NotNull
    private ObjectId userId;

    @NotNull
    @Size(min = 1, max = 500)
    private String content;

    private LocalDateTime createdAt;
}

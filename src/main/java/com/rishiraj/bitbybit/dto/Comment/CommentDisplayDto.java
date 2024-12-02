package com.rishiraj.bitbybit.dto.Comment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDisplayDto {
    private String content;
    private String username;
    private LocalDateTime createdAt;
}

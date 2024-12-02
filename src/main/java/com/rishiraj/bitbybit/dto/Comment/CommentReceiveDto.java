package com.rishiraj.bitbybit.dto.Comment;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class CommentReceiveDto {
    private String Content;
    private ObjectId courseId;
}

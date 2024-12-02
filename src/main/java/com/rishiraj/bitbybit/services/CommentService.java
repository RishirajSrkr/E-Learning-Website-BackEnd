package com.rishiraj.bitbybit.services;

import com.rishiraj.bitbybit.dto.Comment.CommentDisplayDto;
import com.rishiraj.bitbybit.dto.Comment.CommentReceiveDto;
import org.bson.types.ObjectId;

import java.util.List;

public interface CommentService {
   String postComment(CommentReceiveDto commentDto);

   List<CommentDisplayDto> showAllComments(ObjectId courseId);
}

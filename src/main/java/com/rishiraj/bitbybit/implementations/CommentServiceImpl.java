package com.rishiraj.bitbybit.implementations;


import com.rishiraj.bitbybit.customExceptions.UserNotFoundException;
import com.rishiraj.bitbybit.dto.Comment.CommentDisplayDto;
import com.rishiraj.bitbybit.dto.Comment.CommentReceiveDto;
import com.rishiraj.bitbybit.entity.Comment;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.repositories.CommentRepository;
import com.rishiraj.bitbybit.repositories.UserRepository;
import com.rishiraj.bitbybit.services.CommentService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;


    @Override
    public String postComment(CommentReceiveDto commentDto) {
        if (commentDto == null || commentDto.getContent().isEmpty()) {
            //it means the comment is blank, do not post it
            return null;
        }

        //get the logged-in user and pass the userId to comment object
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found for email :: " + email));

        //creating the comment object from commentDto
        Comment comment = Comment.builder()
                .courseId(commentDto.getCourseId())
                .content(commentDto.getContent())
                .userId(user.getId())
                .createdAt(LocalDateTime.now())
                .replies(new ArrayList<>())
                .build();

        commentRepository.save(comment);

        return comment.getContent();

    }

    @Override
    public List<CommentDisplayDto> showAllComments(ObjectId courseId) {

        List<Comment> comments = commentRepository.findByCourseIdOrderByCreatedAtAsc(courseId);

        return comments.stream().map(comment -> {
            return CommentDisplayDto.builder()
                    .content(comment.getContent())
                    .username(userRepository.findById(comment.getUserId()).get().getName())
                    .createdAt(comment.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }
}

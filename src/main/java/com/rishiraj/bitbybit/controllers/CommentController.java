package com.rishiraj.bitbybit.controllers;

import com.rishiraj.bitbybit.dto.Comment.CommentDisplayDto;
import com.rishiraj.bitbybit.dto.Comment.CommentReceiveDto;
import com.rishiraj.bitbybit.services.CommentService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> postComment(@Valid @RequestBody CommentReceiveDto commentDto) {

        String commentContent = commentService.postComment(commentDto);
        if (commentContent == null) {
            return new ResponseEntity<>(Map.of("message", "Comment cannot be blank"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(Map.of("message", "Comment posted successfully"), HttpStatus.OK);
    }


    @GetMapping("/{courseId}")
    public ResponseEntity<List<CommentDisplayDto>> getAllComments(@PathVariable ObjectId courseId) {
        List<CommentDisplayDto> commentDisplayDtos = commentService.showAllComments(courseId);
        return new ResponseEntity<>(commentDisplayDtos, HttpStatus.OK);
    }
}

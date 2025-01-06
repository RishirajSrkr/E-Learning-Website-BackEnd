package com.rishiraj.bitbybit.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Indexed
    private ObjectId id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    //our authentication logics will be based on email so it's good to have indexing here, since we will search users via email.
    @Indexed(unique = true)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    private String profileImageUrl;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 4,  message = "Password must be at least 4 characters long")
    private String password;

    //taking some info of the user to show relevant content to the user
    @NotBlank(message = "Please write something about yourself")
    private String bio;

    @DBRef
    private List<Course> uploadedCourses = new ArrayList<>();

    //using set to prevent duplicate roles
    private Set<String> roles = new HashSet<>();

    @DBRef
    private List<Course> votedCourses = new ArrayList<>();

    //total votes the user has got in his uploaded courses.
    private int totalVotes;
}

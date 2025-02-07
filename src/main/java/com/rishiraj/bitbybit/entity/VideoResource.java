package com.rishiraj.bitbybit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResource {
    @Id
    private String id;
    private String videoUrl;
    private String description;
}

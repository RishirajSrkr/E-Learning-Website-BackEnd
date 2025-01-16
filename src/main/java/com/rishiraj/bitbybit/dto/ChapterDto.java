package com.rishiraj.bitbybit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterDto {
    @JsonProperty("chapterTitle")
    private String chapterName;
    private String chapterContent;
    private ObjectId courseId;
    private String videoLink;
}

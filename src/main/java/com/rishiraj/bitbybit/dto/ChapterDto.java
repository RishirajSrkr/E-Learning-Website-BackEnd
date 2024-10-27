package com.rishiraj.bitbybit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class ChapterDto {
    @JsonProperty("chapterTitle")
    private String chapterName;
    private String chapterContent;
    private ObjectId courseId;
}

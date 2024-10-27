package com.rishiraj.bitbybit.controllers;

import com.rishiraj.bitbybit.entity.Chapter;
import com.rishiraj.bitbybit.implementations.ChapterServicesImpl;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chapter")
public class ChapterControllers {

    @Autowired
    private ChapterServicesImpl chapterServices;

    @PostMapping("/add")
    public void addChapter(Chapter chapter, ObjectId courseId){
        chapterServices.addChapter(chapter, courseId);
    }
}

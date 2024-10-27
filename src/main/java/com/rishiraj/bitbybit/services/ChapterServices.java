package com.rishiraj.bitbybit.services;

import com.rishiraj.bitbybit.entity.Chapter;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ChapterServices {

    public void addChapter(Chapter newChapter, ObjectId courseId);

    public Optional<Chapter> findChapterById(ObjectId chapterId) ;
}

package com.rishiraj.bitbybit.implementations;

import com.rishiraj.bitbybit.entity.Chapter;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.repositories.ChapterRepository;
import com.rishiraj.bitbybit.services.ChapterServices;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ChapterServicesImpl implements ChapterServices {

    private static final Logger log = LoggerFactory.getLogger(ChapterServicesImpl.class);

    private final ChapterRepository chapterRepository;
    private final CourseServicesImpl courseServices;

    public ChapterServicesImpl(ChapterRepository chapterRepository,  CourseServicesImpl courseServices) {
        this.chapterRepository = chapterRepository;
        this.courseServices = courseServices;
    }

    //adding new chapter
    @Transactional
    public void addChapter(Chapter newChapter, ObjectId courseId) {
        try {
            //find the chapter
            Optional<Chapter> chapterOptional = findChapterById(newChapter.getId());
            Chapter chapter = chapterOptional.orElseThrow(() -> new Exception("Exception while adding chapter : ID : " + courseId));
            //assign default values
            chapter.setCreatedAt(LocalDateTime.now());
            //save chapter to db
            chapterRepository.save(chapter);
            //find the course by courseId and add the chapter to the course
            Optional<Course> courseOptional = courseServices.findCourseById(courseId);
            Course course = courseOptional.orElseThrow(() -> new Exception("Exception while adding chapter : ID : " + courseId));
            course.getChapters().add(chapter);
            //update the user
            courseServices.updateCourse(course);
        } catch (Exception e) {
            log.error("Exception while adding");
        }
    }

    public Optional<Chapter> findChapterById(ObjectId chapterId) {
        return chapterRepository.findById(chapterId);
    }
}

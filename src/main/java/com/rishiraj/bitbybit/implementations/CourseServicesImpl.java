package com.rishiraj.bitbybit.implementations;

import com.rishiraj.bitbybit.customExceptions.AlreadyVotedException;
import com.rishiraj.bitbybit.customExceptions.CourseNotFoundException;
import com.rishiraj.bitbybit.customExceptions.UserNotFoundException;
import com.rishiraj.bitbybit.dto.CourseDto;
import com.rishiraj.bitbybit.entity.Chapter;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.repositories.ChapterRepository;
import com.rishiraj.bitbybit.repositories.CourseRepository;
import com.rishiraj.bitbybit.repositories.UserRepository;
import com.rishiraj.bitbybit.services.CourseServices;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseServicesImpl implements CourseServices {

    private static final Logger log = LoggerFactory.getLogger(CourseServicesImpl.class);
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;
    private final CloudinaryImageUploadServiceImpl imageUploadService;

    public CourseServicesImpl(CourseRepository courseRepository, UserRepository userRepository, ChapterRepository chapterRepository, CloudinaryImageUploadServiceImpl imageUploadService) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.chapterRepository = chapterRepository;
        this.imageUploadService = imageUploadService;
    }

    public Optional<Course> findCourseById(ObjectId courseId) {
        return courseRepository.findById(courseId);
    }

    public void updateCourse(Course course) {
        courseRepository.save(course);
    }


    /* GET THE COURSES UPLOADED BY A USER */

    public List<Course> getAllCourseUploadedByUser(ObjectId userId) throws Exception {
        try {
            List<Course> allCourses = courseRepository.findAll();
            List<Course> coursesUploadedByUser;
            coursesUploadedByUser = allCourses.stream()
                    .filter(course -> course.getCreatedBy().equals(userId))
                    .collect(Collectors.toList());

            return coursesUploadedByUser;
        } catch (Exception e) {
            throw new Exception();
        }

    }

    /* GET THE COURSES ENROLLED BY A USER */

    public List<Course> getAllCoursesEnrolledByUser(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            return userOptional.get().getEnrolledCourses();
        }
        return Collections.emptyList();
    }



    /* CREATE A NEW COURSE */

    public Course createCourse(CourseDto courseDto, MultipartFile courseImage, String email) throws IOException {

        /* user will always exist because we got the email from  authentication object */
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found for email : " + email));

        //upload image to cloudinary and get the URL
        String imageUrl = null;
        if (courseImage != null && !courseImage.isEmpty()) {
            Map uploadResult = imageUploadService.uploadFile(courseImage);
            imageUrl = (String) uploadResult.get("url");

            log.info("----------------------------------------");
            log.info("uploadResult {} ", uploadResult);
            log.info("----------------------------------------");

        }

        //converting the CourseDto to actual course object
        Course course = Course.builder()
                .courseName(courseDto.getCourseName())
                .courseDescription(courseDto.getCourseDescription())
                .courseCategory(courseDto.getCourseCategory())
                .createdAt(LocalDateTime.now())
                .createdBy(user.getId())
                .instructorName(user.getName())
                .vote(0)
                .numberOfEnrolls(0)
                .imageUrl(imageUrl)
                .build();


        Course savedCourse = courseRepository.save(course);

        /*
        adding the course to user's uploaded course List
        updating the user
         */
        user.getUploadedCourse().add(savedCourse);
        userRepository.save(user);


        /*
        we saved course first in db, so that we have a valid course id which is needed in chapter,
        in chapter we have to add the courseId, so we saved course first to get courseId
         */

        /*
        since chapters expect List of chapters, but CourseDto contains List of chapterDto
        so converting List<ChapterDto> to List<Chapter>
         */
        List<Chapter> chapters = courseDto.getChapters()
                .stream().map(chapterDto -> Chapter.builder()
                        .chapterName(chapterDto.getChapterName())
                        .chapterContent(chapterDto.getChapterContent())
                        .createdAt(LocalDateTime.now())
                        .courseId(savedCourse.getId())
                        .build()
                ).collect(Collectors.toList());


        List<Chapter> savedChapters = chapterRepository.saveAll(chapters);

        /*
        updating the course with the chapters
         */
        savedCourse.setChapters(savedChapters);

        return courseRepository.save(savedCourse);

    }


    /* FIND A COURSE BY NAME */

    public Course getCourseByName(String courseName) {
        return courseRepository.findByCourseName(courseName).orElseThrow(() -> new RuntimeException("Course not found with name : " + courseName));
    }


    /* DELETE A COURSE
    - a user can delete a course which he has uploaded or created, not some other user courses.
    - we need to delete the chapter associated with the course from chapter table as well.
    */
    @Transactional
    public void deleteCourse(ObjectId courseId, User user) throws AccessDeniedException {
         /*
        check whether the courseId / course was uploaded by the user or not
         */
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found with course ID : " + courseId));
        if (!course.getCreatedBy().equals(user.getId())) {

            log.warn("User [{}] attempted to delete course [{}] that doesn't belong to the user.", user.getId(), courseId);

            throw new AccessDeniedException("Failed to delete the course :: You may not have the permission to delete it.");
        }

        /*
        this will delete all the chapter from chapter collection which has this courseId
         */
        chapterRepository.deleteByCourseId(courseId);

       /*
       deleting the course from course collection
        */
        courseRepository.deleteById(courseId);

        log.info("Course [{}] and its chapters have been successfully deleted.", courseId);

    }



    /* INCREMENT THE NUMBER OF ENROLLS, WHEN SOMEONE ENROLL THE COURSE */

    public void incrementNumberOfEnrolls(ObjectId courseId) throws CourseNotFoundException {
        try {
            Optional<Course> optionalCourse = courseRepository.findById(courseId);
            if (optionalCourse.isPresent()) {
                Course course = optionalCourse.get();
                int numberOfEnrolls = course.getNumberOfEnrolls();
                course.setNumberOfEnrolls(numberOfEnrolls + 1);
                courseRepository.save(course);
            } else {
                throw new CourseNotFoundException("Course not found with ID: " + courseId);
            }
        } catch (CourseNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while incrementing enrolls for course ID: " + courseId, e);
        }

    }



    /* ADD COURSE TO ENROLLED COURSE LIST */

    @Transactional
    public void addCourseToUsersEnrolledCourses(ObjectId courseId, String userEmail) throws CourseNotFoundException, UserNotFoundException {

        Course enrolledCourse = findCourseById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with the ID: " + courseId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with the email: " + userEmail));

        //check if user is already enrolled
        if (!user.getEnrolledCourses().contains(enrolledCourse)) {
            user.getEnrolledCourses().add(enrolledCourse);
            userRepository.save(user);
        }
    }


    /* GET ALL COURSES
    If a user is logged in and has uploaded some course or some enrolled courses, do not show those courses in 'all courses'
    If a user is not logged in than show all courses here
     */
    public List<Course> getAllCourses(String email) throws Exception {

        List<Course> allCourses = courseRepository.findAll();
        /*
        if email is null, it means no user is logged-in, so show all courses to the user
         */
        if (email.equals("anonymousUser")) {
            return allCourses;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));

        List<Course> coursesUploadedByUser = allCourses.stream().filter(course -> course.getCreatedBy().equals(user.getId())).collect(Collectors.toList());

        //removing courses uploaded by the user
        allCourses.removeAll(coursesUploadedByUser);

        //removing courses enrolled by the user
        allCourses.removeAll(getAllCoursesEnrolledByUser(email));

        return allCourses;


    }


    /*
    TOP 3 VOTED COURSES
     */
    public List<Course> getTopVotedCourses(){
        int count = 3;
        List<Course> allCourses = courseRepository.findAll();
        Collections.sort(allCourses);
        List<Course> topVotedCourses = allCourses.subList(0, 3);
        return topVotedCourses;
    }


    /*
    REMOVE ENROLLED COURSE
     */
    public void removeEnrolledCourse(ObjectId courseId){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email : " + email + " not found"));
        List<Course> collect = user.getEnrolledCourses().stream().filter(course -> !course.getId().equals(courseId)).collect(Collectors.toList());
        user.setEnrolledCourses(collect);
        userRepository.save(user);
    }

}

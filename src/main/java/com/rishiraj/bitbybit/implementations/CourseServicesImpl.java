package com.rishiraj.bitbybit.implementations;

import com.rishiraj.bitbybit.customExceptions.CourseNotFoundException;
import com.rishiraj.bitbybit.customExceptions.UserNotFoundException;
import com.rishiraj.bitbybit.dto.ChapterDto;
import com.rishiraj.bitbybit.dto.Course.CourseDto;
import com.rishiraj.bitbybit.dto.Course.CourseDtoWithEnrolledUsers;
import com.rishiraj.bitbybit.dto.User.UserDto;
import com.rishiraj.bitbybit.dto.User.UserWithCoursesDto;
import com.rishiraj.bitbybit.entity.Chapter;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.repositories.ChapterRepository;
import com.rishiraj.bitbybit.repositories.CourseRepository;
import com.rishiraj.bitbybit.repositories.EnrollmentRepository;
import com.rishiraj.bitbybit.repositories.UserRepository;
import com.rishiraj.bitbybit.services.ResourceService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseServicesImpl implements ResourceService {

    private static final Logger log = LoggerFactory.getLogger(CourseServicesImpl.class);
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;
    private final CloudinaryImageUploadServiceImpl imageUploadService;
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public CourseServicesImpl(CourseRepository courseRepository, UserRepository userRepository, ChapterRepository chapterRepository, CloudinaryImageUploadServiceImpl imageUploadService) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.chapterRepository = chapterRepository;
        this.imageUploadService = imageUploadService;
    }

    public Optional<Course> findCourseById(ObjectId resourceId) {
        return courseRepository.findById(resourceId);
    }

    public void updateCourse(Course resource) {
        courseRepository.save(resource);
    }


    /* GET THE COURSES UPLOADED BY A USER */

    public List<Course> getAllCourseUploadedByUser(ObjectId userId) throws Exception {
        try {
            return courseRepository.findAll().stream().filter(course -> course.getCreatedBy().equals(userId)).collect(Collectors.toList());

        } catch (
                Exception e) {
            throw new Exception();
        }

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
        }

        log.info("user email {}", email);
        //converting the CourseDto to actual course object
        Course course = Course.builder()
                .courseName(courseDto.getCourseName())
                .courseDescription(courseDto.getCourseDescription())
                .courseCategory(courseDto.getCourseCategory())
                .createdAt(LocalDateTime.now())
                .createdBy(user.getId())
                .instructorEmail(user.getEmail())
                .votes(0)
                .numberOfEnrolls(0)
                .imageUrl(imageUrl)
                .build();


        Course savedCourse = courseRepository.save(course);

        /*
        adding the course to user's uploaded course List
        updating the user
         */
        user.getUploadedCourses().add(savedCourse);
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
                        .videoLink(chapterDto.getVideoLink())
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
        } catch (
                CourseNotFoundException e) {
            throw e;
        } catch (
                Exception e) {
            throw new RuntimeException("An error occurred while incrementing enrolls for course ID: " + courseId, e);
        }

    }


    /* GET ALL COURSES
    If a user is logged in and has uploaded some course or some enrolled courses, do not show those courses in 'all courses'
    If a user is not logged in than show all courses here
     */
    public List<Course> getFilteredCourses(String email) throws Exception {

        List<Course> allCourses = courseRepository.findAll();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found!"));

        //removing the courses uploaded by the user
        List<Course> filteredCourses = allCourses.stream().filter(courses -> !courses.getCreatedBy().equals(user.getId())).collect(Collectors.toList());


        //removing the courses enrolled by the user
        List<Course> coursesEnrolledByUser = enrollmentRepository.findAll().stream().filter(enrollment -> enrollment.getUser().getId().equals(user.getId())).map(enrollment -> enrollment.getCourse()).collect(Collectors.toList());
        if (!coursesEnrolledByUser.isEmpty()) {
            filteredCourses.removeAll(coursesEnrolledByUser);
            return filteredCourses;
        }

        return filteredCourses;
    }


    /*
    TOP 3 VOTED COURSES
     */
    public List<Course> getTopVotedCourses() {
        int count = 3;
        List<Course> allCourses = courseRepository.findAll();
        Collections.sort(allCourses);

        //if there are less than 2 courses in database, return all 2 without doing "subList(0,3)
        if (allCourses.size() < 3)
            return allCourses;

        //else return top 3, i.e. subList(0,3)
        return allCourses.subList(0, 3);
    }


    public CourseDto convertToCourseDto(Course course) {

        return CourseDto.builder()
                .courseName(course.getCourseName())
                .courseDescription(course.getCourseDescription())
                .courseCategory(course.getCourseCategory())
                .chapters(course.getChapters().stream()
                        .map(chapter -> ChapterDto.builder()
                                .chapterName(chapter.getChapterName())
                                .chapterContent(chapter.getChapterContent())
                                .courseId(chapter.getCourseId())
                                .build()
                        ).collect(Collectors.toList()))
                .build();
    }

    public CourseDtoWithEnrolledUsers convertToCourseWithEnrolledUsersDto(Course course) {

        return CourseDtoWithEnrolledUsers.builder()
                .courseName(course.getCourseName())
                .courseDescription(course.getCourseDescription())
                .courseCategory(course.getCourseCategory())
                .createdAt(course.getCreatedAt())
                .createdBy(course.getCreatedBy())
                .courseImage(course.getImageUrl())
                .votes(course.getVotes())
                .chapters(course.getChapters())
//                .enrolledBy(course.getEnrolledBy().stream().map(user -> convertToUserDto(user)).collect(Collectors.toList()))
                .build();
    }

    private UserDto convertToUserDto(User user) {

        return UserDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bio(user.getBio())
                .profileImage(user.getProfileImageUrl())
                .build();

    }

    public UserWithCoursesDto convertToUserWithCourseDto(User user) {
        return UserWithCoursesDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bio(user.getBio())
                .profileImage(user.getProfileImageUrl())
                .uploadedCourses(user.getUploadedCourses().stream().map(course -> convertToCourseDto(course)).collect(Collectors.toList()))
                .build();
    }

}

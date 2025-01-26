package com.rishiraj.bitbybit.implementations;

import com.rishiraj.bitbybit.customExceptions.CourseNotFoundException;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.Enrollment;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.repositories.CourseRepository;
import com.rishiraj.bitbybit.repositories.EnrollmentRepository;
import com.rishiraj.bitbybit.repositories.UserRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    UserRepository userRepository;


    public void enrollUserInCourse(User user, ObjectId courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException("Course with ID :: " + courseId + "not found"));
        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .user(user)
                .enrolledAt(LocalDateTime.now())
                .build();

        log.info("Enrollment object :: {}", enrollment);
        enrollmentRepository.save(enrollment);

        course.setNumberOfEnrolls(course.getNumberOfEnrolls() + 1);
        courseRepository.save(course);
    }

    public List<User> getEnrolledUsers(ObjectId courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourse(courseId);
        return enrollments.stream().map(enrollment -> enrollment.getUser()).collect(Collectors.toList());

    }

    public List<Course> getEnrolledCourses(ObjectId userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUser(userId);
        return enrollments.stream().map(enrollment -> enrollment.getCourse()).collect(Collectors.toList());

    }

    public List<String> getEnrolledCoursesObjectIds(ObjectId userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUser(userId);
        return enrollments.stream().map(enrollment -> enrollment.getCourse().getId().toString()).collect(Collectors.toList());
    }


    public void removeCourseFromEnrollment(User user, Course course) throws AccessDeniedException {
        //check if the course belongs to the user
        List<Enrollment> collect = enrollmentRepository.findByUser(user.getId()).stream().filter(enrollment -> enrollment.getCourse().getId().equals(course.getId())).collect(Collectors.toList());
        if (collect.isEmpty()) {
            throw new AccessDeniedException("You do not have the permission to perform the action!");
        } else {
            Enrollment enrollmentToRemove = collect.stream().findFirst().get();
            enrollmentRepository.delete(enrollmentToRemove);

            course.setNumberOfEnrolls(course.getNumberOfEnrolls() - 1);
            courseRepository.save(course);
        }
    }
}

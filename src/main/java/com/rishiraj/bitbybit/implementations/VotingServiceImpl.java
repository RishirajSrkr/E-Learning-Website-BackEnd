package com.rishiraj.bitbybit.implementations;

import com.rishiraj.bitbybit.customExceptions.CourseNotFoundException;
import com.rishiraj.bitbybit.customExceptions.UserNotFoundException;
import com.rishiraj.bitbybit.entity.Course;
import com.rishiraj.bitbybit.entity.User;
import com.rishiraj.bitbybit.repositories.CourseRepository;
import com.rishiraj.bitbybit.repositories.UserRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class VotingServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(VotingServiceImpl.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public VotingServiceImpl(RedisTemplate<String, Object> redisTemplate, CourseRepository courseRepository, UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public void voteCourse(ObjectId courseId) {

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException("Course with ID: " + courseId + " not found"));
        ObjectId createdBy = course.getCreatedBy();


        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email : " + email + " not found"));

        if (hasUserVoted(courseId, user.getId())) {
            throw new IllegalStateException("User has already voted for this course.");
        }

        // Increment the vote count in Redis for the course
        redisTemplate.opsForHash().increment("courseVotes", courseId.toHexString(), 1);

        //Increment the total vote count in Redis for the user
        redisTemplate.opsForHash().increment("userTotalVotes", createdBy.toHexString(), 1);

        // Track the user's vote and set TTL for 24 hours (each user's vote tracked individually)
        String userVoteKey = "userVote:" + courseId.toHexString() + ":" + user.getId().toHexString();

        redisTemplate.opsForValue().set(userVoteKey, "voted", Duration.ofDays(1));


        //add the course in users votedCourse list
        user.getVotedCourses().add(course);

        userRepository.save(user);

    }

    private boolean hasUserVoted(ObjectId courseId, ObjectId userId) {
        String userVoteKey = "userVote:" + courseId.toHexString() + ":" + userId.toHexString();
        return Boolean.TRUE.equals(redisTemplate.hasKey(userVoteKey));
    }


    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void persistVotesToDatabase() {

        // Fetch all course votes from Redis and save them to the database
        Map<Object, Object> voteMap = redisTemplate.opsForHash().entries("courseVotes");
        voteMap.forEach((key, value) -> {
            Course course = courseRepository.findById(new ObjectId((String) key)).orElseThrow();
            course.setVotes((Integer) value);
            courseRepository.save(course);
        });

        Map<Object, Object> userTotalVoteMap = redisTemplate.opsForHash().entries("userTotalVotes");
        userTotalVoteMap.forEach((key, value) -> {
            User user = userRepository.findById(new ObjectId(key.toString())).orElseThrow();
            user.setTotalVotes((Integer)value);
            userRepository.save(user);
        });

        log.info("Saved to database :: {} ", LocalDateTime.now());

        redisTemplate.delete(List.of("courseVotes", "userTotalVotes"));

    }


}

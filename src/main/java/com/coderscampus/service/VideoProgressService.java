package com.coderscampus.service;

import com.coderscampus.domain.Student;
import com.coderscampus.domain.VideoProgress;
import com.coderscampus.repository.StudentRepository;
import com.coderscampus.repository.VideoProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class VideoProgressService {

    private static final Logger logger = LoggerFactory.getLogger(VideoProgressService.class);

    private final VideoProgressRepository videoProgressRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public VideoProgressService(VideoProgressRepository videoProgressRepository, StudentRepository studentRepository) {
        this.videoProgressRepository = videoProgressRepository;
        this.studentRepository = studentRepository;
    }
    
    public List<VideoProgress> getVideoProgressForStudent(Long studentId) {
        try {
            logger.debug("Fetching video progress for student ID: {}", studentId);
            if (studentId == null) {
                logger.warn("Attempted to fetch video progress with null studentId");
                return new ArrayList<>();
            }
            return videoProgressRepository.findByStudentIdOrderByWeekNumberAsc(studentId);
        } catch (Exception e) {
            logger.error("Error fetching video progress for student ID {}: {}", studentId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Transactional
    public VideoProgress createVideoProgress(Long studentId, VideoProgress videoProgress) {
        try {
            logger.debug("Creating video progress for student ID: {}, week: {}", 
                     studentId, videoProgress != null ? videoProgress.getWeekNumber() : "null");
            
            if (studentId == null) {
                logger.error("Cannot create video progress: studentId is null");
                throw new IllegalArgumentException("Student ID cannot be null");
            }
            
            if (videoProgress == null) {
                logger.error("Cannot create video progress: videoProgress object is null");
                throw new IllegalArgumentException("VideoProgress cannot be null");
            }
            
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> {
                        logger.error("Student not found with ID: {}", studentId);
                        return new IllegalArgumentException("Student not found with id: " + studentId);
                    });
            
            videoProgress.setStudent(student);
            
            // Update student's total Vimeo hours watched
            Double totalHours = calculateTotalHoursWatched(student);
            if (totalHours == null) totalHours = 0.0;
            
            double hoursWatched = videoProgress.getHoursWatched() != null ? videoProgress.getHoursWatched() : 0.0;
            student.setVimeoHoursWatched(totalHours + hoursWatched);
            
            studentRepository.save(student);
            VideoProgress savedProgress = videoProgressRepository.save(videoProgress);
            logger.info("Created video progress for student ID: {}, week: {}, hours: {}", 
                      studentId, videoProgress.getWeekNumber(), hoursWatched);
            return savedProgress;
        } catch (Exception e) {
            logger.error("Error creating video progress for student ID {}: {}", studentId, e.getMessage(), e);
            throw e;
        }
    }
    
    private Double calculateTotalHoursWatched(Student student) {
        try {
            if (student == null || student.getId() == null) {
                logger.warn("Cannot calculate total hours watched: student or student ID is null");
                return 0.0;
            }
            
            List<VideoProgress> progressEntries = videoProgressRepository.findByStudentIdOrderByWeekNumberAsc(student.getId());
            return progressEntries.stream()
                    .mapToDouble(vp -> vp.getHoursWatched() != null ? vp.getHoursWatched() : 0.0)
                    .sum();
        } catch (Exception e) {
            logger.error("Error calculating total hours watched for student {}: {}", 
                       student != null ? student.getName() : "null", e.getMessage(), e);
            return 0.0;
        }
    }
    
    @Transactional
    public void createInitialVideoProgressForStudent(Student student, int numberOfWeeks) {
        try {
            if (student == null) {
                logger.error("Cannot create initial video progress: student is null");
                return;
            }
            
            logger.info("Creating initial video progress for student: {}, ID: {}, weeks: {}", 
                     student.getName(), student.getId(), numberOfWeeks);
            
            if (student.getStartDate() == null) {
                logger.info("Setting missing start date for student: {}", student.getName());
                student.setStartDate(LocalDate.now().minusWeeks(numberOfWeeks));
            }
            
            // Check if student already has video progress
            List<VideoProgress> existingProgress = videoProgressRepository.findByStudentIdOrderByWeekNumberAsc(student.getId());
            if (existingProgress != null && !existingProgress.isEmpty()) {
                logger.info("Student already has {} video progress entries, skipping creation", existingProgress.size());
                return;
            }
            
            double totalHours = 0.0;
            
            for (int week = 1; week <= numberOfWeeks; week++) {
                // Generate random hours between 2 and 6 for demo data
                double hoursWatched = 2.0 + Math.random() * 4.0;
                hoursWatched = Math.round(hoursWatched * 10.0) / 10.0; // Round to 1 decimal place
                
                VideoProgress weekProgress = new VideoProgress();
                weekProgress.setStudent(student);
                weekProgress.setWeekNumber(week);
                weekProgress.setHoursWatched(hoursWatched);
                
                videoProgressRepository.save(weekProgress);
                totalHours += hoursWatched;
                
                logger.debug("Created video progress for week {}: {} hours", week, hoursWatched);
            }
            
            student.setVimeoHoursWatched(totalHours);
            studentRepository.save(student);
            
            logger.info("Successfully created {} weeks of video progress for student: {}, total hours: {}", 
                      numberOfWeeks, student.getName(), totalHours);
        } catch (Exception e) {
            logger.error("Error creating initial video progress for student {}: {}", 
                       student != null ? student.getName() : "null", e.getMessage(), e);
        }
    }
} 
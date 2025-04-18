package com.coderscampus.service;

import com.coderscampus.domain.*;
import com.coderscampus.repository.MilestoneRepository;
import com.coderscampus.repository.ProfileSettingsRepository;
import com.coderscampus.repository.ReportRepository;
import com.coderscampus.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DummyDataService {

    private static final Logger logger = LoggerFactory.getLogger(DummyDataService.class);

    private final StudentRepository studentRepository;
    private final ReportRepository reportRepository;
    private final MilestoneRepository milestoneRepository;
    private final ProfileSettingsRepository profileSettingsRepository;
    private final VideoProgressService videoProgressService;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    @Autowired
    public DummyDataService(StudentRepository studentRepository,
                           ReportRepository reportRepository,
                           MilestoneRepository milestoneRepository,
                           ProfileSettingsRepository profileSettingsRepository,
                           VideoProgressService videoProgressService,
                           PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.reportRepository = reportRepository;
        this.milestoneRepository = milestoneRepository;
        this.profileSettingsRepository = profileSettingsRepository;
        this.videoProgressService = videoProgressService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createDemoStudents() {
        try {
            if (studentRepository.count() == 0) {
                // Create 5 demo students
                logger.info("Creating demo students from scratch");
                createDemoStudent("John Smith", "john.smith@example.com", LocalDate.now().minusWeeks(8));
                createDemoStudent("Emily Johnson", "emily.johnson@example.com", LocalDate.now().minusWeeks(12));
                createDemoStudent("Michael Lee", "michael.lee@example.com", LocalDate.now().minusWeeks(6));
                createDemoStudent("Sofia Rodriguez", "sofia.rodriguez@example.com", LocalDate.now().minusWeeks(4));
                createDemoStudent("Alex Chen", "alex.chen@example.com", LocalDate.now().minusWeeks(10));
                logger.info("Demo students created successfully");
            }
        } catch (Exception e) {
            logger.error("Error creating demo students: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public Student createDemoStudent(String name, String email, LocalDate startDate) {
        try {
            logger.info("Creating demo student: {}", name);
            Student student = new Student();
            student.setName(name);
            student.setEmail(email);
            student.setPassword(passwordEncoder.encode("password"));
            
            // Ensure start date is set for all students
            student.setStartDate(startDate);
            student.setIsDemo(true);
            
            // Calculate weeks in bootcamp
            if (startDate != null) {
                long weeksBetween = ChronoUnit.WEEKS.between(startDate, LocalDate.now());
                student.setWeeksInBootcamp((int) weeksBetween);
            } else {
                student.setWeeksInBootcamp(1);
            }
            
            // Initialize primitive fields to avoid null
            if (student.getAssignmentsExpected() == null) student.setAssignmentsExpected(0);
            if (student.getAssignmentsSubmitted() == null) student.setAssignmentsSubmitted(0);
            if (student.getVimeoHoursWatched() == null) student.setVimeoHoursWatched(0.0);
            
            student = studentRepository.save(student);
            logger.info("Saved student with ID: {}", student.getId());
            
            // Create profile settings
            createProfileSettingsForStudent(student);
            
            // Create milestones
            createMilestonesForStudent(student);
            
            // Create assignments and video progress
            generateAssignmentData(student);
            int weeksInBootcamp = student.getWeeksInBootcamp() != null ? student.getWeeksInBootcamp() : 4;
            
            logger.info("Creating video progress for student ID: {}, weeks: {}", student.getId(), weeksInBootcamp);
            videoProgressService.createInitialVideoProgressForStudent(student, weeksInBootcamp);
            
            // Generate reports
            createReportsForStudent(student);
            
            return studentRepository.save(student);
        } catch (Exception e) {
            logger.error("Error creating demo student {}: {}", name, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional
    public void addDummyDataToExistingStudent(Student student) {
        try {
            logger.info("Adding dummy data to existing student: {}, ID: {}", student.getName(), student.getId());
            
            // Ensure start date is set for all students
            if (student.getStartDate() == null) {
                logger.info("Setting missing start date for student: {}", student.getName());
                student.setStartDate(LocalDate.now().minusWeeks(4));
            }
            
            // Skip if student already has dummy data
            if (student.getReports() != null && !student.getReports().isEmpty()) {
                logger.info("Student already has reports, skipping: {}", student.getName());
                return;
            }
            
            // Set/recalculate weeksInBootcamp based on start date
            long weeksBetween = ChronoUnit.WEEKS.between(student.getStartDate(), LocalDate.now());
            student.setWeeksInBootcamp((int) weeksBetween);
            logger.info("Set weeks in bootcamp: {}", student.getWeeksInBootcamp());
            
            // Initialize primitive fields to avoid null
            if (student.getAssignmentsExpected() == null) student.setAssignmentsExpected(0);
            if (student.getAssignmentsSubmitted() == null) student.setAssignmentsSubmitted(0);
            if (student.getVimeoHoursWatched() == null) student.setVimeoHoursWatched(0.0);
            
            // Create profile settings if not exists
            if (student.getProfileSettings() == null) {
                logger.info("Creating profile settings for student: {}", student.getName());
                createProfileSettingsForStudent(student);
            }
            
            // Create milestones if not exists
            if (student.getMilestones() == null || student.getMilestones().isEmpty()) {
                logger.info("Creating milestones for student: {}", student.getName());
                createMilestonesForStudent(student);
            }
            
            // Generate assignment data
            logger.info("Generating assignment data for student: {}", student.getName());
            generateAssignmentData(student);
            
            // Create video progress if not exists
            if (student.getVideoProgressList() == null || student.getVideoProgressList().isEmpty()) {
                int weeksInBootcamp = student.getWeeksInBootcamp() != null ? student.getWeeksInBootcamp() : 4;
                logger.info("Creating video progress for student: {}, weeks: {}", student.getName(), weeksInBootcamp);
                videoProgressService.createInitialVideoProgressForStudent(student, weeksInBootcamp);
            }
            
            // Create reports if not exists
            if (student.getReports() == null || student.getReports().isEmpty()) {
                logger.info("Creating reports for student: {}", student.getName());
                createReportsForStudent(student);
            }
            
            Student updatedStudent = studentRepository.save(student);
            logger.info("Successfully added dummy data to student: {}, ID: {}", updatedStudent.getName(), updatedStudent.getId());
        } catch (Exception e) {
            logger.error("Error adding dummy data to student {}: {}", student.getName(), e.getMessage(), e);
        }
    }
    
    private void createProfileSettingsForStudent(Student student) {
        try {
            ProfileSettings profileSettings = new ProfileSettings();
            profileSettings.setStudent(student);
            profileSettings.setNotificationsEnabled(true);
            profileSettings.setThemePreference("light");
            profileSettings.setBio("I'm a student learning to code at CodersCampus!");
            profileSettings.setGithubUrl("https://github.com/" + student.getName().toLowerCase().replace(" ", ""));
            profileSettings.setLinkedinUrl("https://linkedin.com/in/" + student.getName().toLowerCase().replace(" ", ""));
            
            profileSettingsRepository.save(profileSettings);
            student.setProfileSettings(profileSettings);
            logger.info("Created profile settings for student: {}", student.getName());
        } catch (Exception e) {
            logger.error("Error creating profile settings for student {}: {}", student.getName(), e.getMessage(), e);
        }
    }
    
    private void createMilestonesForStudent(Student student) {
        try {
            LocalDate startDate = student.getStartDate();
            if (startDate == null) {
                startDate = LocalDate.now().minusWeeks(4);
                student.setStartDate(startDate);
            }
            
            List<String> milestoneNames = List.of(
                "Complete HTML/CSS Fundamentals",
                "Build First JavaScript App",
                "Create a React Component",
                "Deploy First Web Application",
                "Complete Java Basics",
                "Build RESTful API",
                "Implement Database Integration",
                "Complete Final Project"
            );
            
            List<Milestone> milestones = new ArrayList<>();
            int totalMilestones = milestoneNames.size();
            int completedMilestones = Math.max(1, student.getWeeksInBootcamp() / 2); // Ensure at least 1 milestone
            
            logger.info("Creating {} milestones with {} completed for student: {}", 
                     totalMilestones, completedMilestones, student.getName());
            
            for (int i = 0; i < totalMilestones; i++) {
                Milestone milestone = new Milestone();
                milestone.setMilestoneName(milestoneNames.get(i));
                milestone.setStudent(student);
                
                // Set some milestones as completed
                if (i < completedMilestones) {
                    milestone.setStatus(Milestone.MilestoneStatus.COMPLETED);
                    milestone.setCompletionDate(startDate.plusWeeks(i * 2));
                    milestone.setComment("Successfully completed this milestone!");
                } else {
                    milestone.setStatus(Milestone.MilestoneStatus.IN_PROGRESS);
                    milestone.setComment("Working on this milestone...");
                }
                
                milestones.add(milestone);
                milestoneRepository.save(milestone);
            }
            
            student.setMilestones(milestones);
            logger.info("Created milestones for student: {}", student.getName());
        } catch (Exception e) {
            logger.error("Error creating milestones for student {}: {}", student.getName(), e.getMessage(), e);
        }
    }
    
    private void generateAssignmentData(Student student) {
        try {
            int weeksInBootcamp = student.getWeeksInBootcamp() != null ? student.getWeeksInBootcamp() : 4;
            
            // Expected assignments (approximately 2 per week)
            int expectedAssignments = weeksInBootcamp * 2;
            
            // Submitted assignments (80-95% completion rate)
            int submittedAssignments = (int) (expectedAssignments * (0.8 + random.nextDouble() * 0.15));
            
            student.setAssignmentsExpected(expectedAssignments);
            student.setAssignmentsSubmitted(submittedAssignments);
            student.setLastAssignmentDate(LocalDate.now().minusDays(random.nextInt(7) + 1));
            
            logger.info("Generated assignment data for student {}: submitted={}, expected={}", 
                       student.getName(), submittedAssignments, expectedAssignments);
        } catch (Exception e) {
            logger.error("Error generating assignment data for student {}: {}", student.getName(), e.getMessage(), e);
        }
    }
    
    private void createReportsForStudent(Student student) {
        try {
            // Create one report per week, starting from enrollment
            int weeksInBootcamp = student.getWeeksInBootcamp() != null ? student.getWeeksInBootcamp() : 4;
            LocalDate startDate = student.getStartDate();
            
            if (startDate == null) {
                startDate = LocalDate.now().minusWeeks(weeksInBootcamp);
                student.setStartDate(startDate);
            }
            
            logger.info("Creating {} weekly reports for student: {}", weeksInBootcamp, student.getName());
            
            List<String> summaryTexts = List.of(
                "Excellent progress! Consistently submitting high-quality work.",
                "Good effort this week, completed all required assignments.",
                "Making steady progress, but needs to increase video watching time.",
                "Submitted most assignments, but quality needs improvement.",
                "Mixed results this week - strong on video content, weaker on assignments."
            );
            
            for (int week = 0; week < weeksInBootcamp; week++) {
                Report weeklyReport = new Report();
                weeklyReport.setStudent(student);
                weeklyReport.setReportDate(startDate.plusWeeks(week));
                
                // Calculate progress up to this point
                int assignmentsExpectedSoFar = (week + 1) * 2;
                int assignmentsSubmittedSoFar = Math.min(
                    student.getAssignmentsSubmitted() * (week + 1) / weeksInBootcamp, 
                    assignmentsExpectedSoFar
                );
                
                // Format assignment summary
                String assignmentSummary = assignmentsSubmittedSoFar + "/" + assignmentsExpectedSoFar + " - " + 
                                         summaryTexts.get(random.nextInt(summaryTexts.size()));
                
                weeklyReport.setAssignmentsSummary(assignmentSummary);
                
                // Calculate Vimeo hours for this week (from VideoProgress)
                double weeklyVimeoHours = (student.getVimeoHoursWatched() != null)
                                      ? (student.getVimeoHoursWatched() / weeksInBootcamp) 
                                      : (2.0 + random.nextDouble() * 3.0);
                
                weeklyReport.setVimeoHoursSummary(Math.round(weeklyVimeoHours * 10.0) / 10.0);
                
                // Calculate engagement score (0-100)
                int assignmentScore = assignmentsExpectedSoFar > 0 
                                  ? (assignmentsSubmittedSoFar * 100 / assignmentsExpectedSoFar) 
                                  : 0;
                int videoScore = (int) (weeklyVimeoHours * 10); // 10 points per hour, max 40-60
                int engagementScore = (assignmentScore + videoScore) / 2;
                engagementScore = Math.min(100, engagementScore); // Cap at 100
                
                weeklyReport.setEngagementScore(engagementScore);
                weeklyReport.setNotes("Week " + (week + 1) + " performance notes. Click to edit.");
                
                reportRepository.save(weeklyReport);
            }
            
            logger.info("Created reports for student: {}", student.getName());
        } catch (Exception e) {
            logger.error("Error creating reports for student {}: {}", student.getName(), e.getMessage(), e);
        }
    }
} 
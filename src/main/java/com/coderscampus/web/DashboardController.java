package com.coderscampus.web;

import com.coderscampus.domain.Milestone;
import com.coderscampus.domain.Report;
import com.coderscampus.domain.Student;
import com.coderscampus.domain.VideoProgress;
import com.coderscampus.service.MilestoneService;
import com.coderscampus.service.ReportService;
import com.coderscampus.service.StudentService;
import com.coderscampus.service.VideoProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private StudentService studentService;
    
    @Autowired
    private MilestoneService milestoneService;
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private VideoProgressService videoProgressService;
    
    @GetMapping("/student-data")
    public ResponseEntity<Map<String, Object>> getStudentDashboardData(Principal principal, 
                                                                      @AuthenticationPrincipal OAuth2User oauth2User) {
        try {
            logger.info("Fetching dashboard data");
            String email = null;
            
            // Handle different authentication types
            if (principal != null) {
                email = principal.getName();
                logger.info("Principal name: {}", email);
            } else if (oauth2User != null) {
                email = oauth2User.getAttribute("email");
                logger.info("OAuth2User email: {}", email);
            }
            
            if (email == null) {
                logger.error("No email found in authentication");
                return ResponseEntity.badRequest().build();
            }
            
            Student student = studentService.getStudentByEmail(email);
            logger.info("Found student: {}, ID: {}", student.getName(), student.getId());
            
            Map<String, Object> dashboardData = new HashMap<>();
            
            // Basic student info
            dashboardData.put("studentId", student.getId());
            dashboardData.put("name", student.getName());
            dashboardData.put("email", student.getEmail());
            dashboardData.put("startDate", student.getStartDate());
            dashboardData.put("weeksInBootcamp", student.getWeeksInBootcamp() != null ? student.getWeeksInBootcamp() : 0);
            
            // Assignment data
            dashboardData.put("assignmentsSubmitted", student.getAssignmentsSubmitted() != null ? student.getAssignmentsSubmitted() : 0);
            dashboardData.put("assignmentsExpected", student.getAssignmentsExpected() != null ? student.getAssignmentsExpected() : 0);
            
            // Calculate completion rate safely
            double completionRate = 0.0;
            if (student.getAssignmentsExpected() != null && student.getAssignmentsExpected() > 0 
                && student.getAssignmentsSubmitted() != null) {
                completionRate = (double) student.getAssignmentsSubmitted() / student.getAssignmentsExpected() * 100;
            }
            dashboardData.put("assignmentCompletionRate", completionRate);
            
            dashboardData.put("lastAssignmentDate", student.getLastAssignmentDate());
            
            // Video progress data
            dashboardData.put("totalVimeoHours", student.getVimeoHoursWatched() != null ? student.getVimeoHoursWatched() : 0.0);
            
            try {
                List<VideoProgress> videoProgressList = videoProgressService.getVideoProgressForStudent(student.getId());
                logger.info("Found {} video progress records", videoProgressList.size());
                
                Map<String, Double> weeklyData = new HashMap<>();
                for (VideoProgress vp : videoProgressList) {
                    if (vp.getWeekNumber() != null && vp.getHoursWatched() != null) {
                        weeklyData.put(vp.getWeekNumber().toString(), vp.getHoursWatched());
                    }
                }
                dashboardData.put("videoProgressByWeek", weeklyData);
            } catch (Exception e) {
                logger.error("Error fetching video progress: {}", e.getMessage(), e);
                dashboardData.put("videoProgressByWeek", new HashMap<>());
            }
            
            // Get 5 most recent reports
            try {
                List<Report> allReports = reportService.getReportsByStudentId(student.getId());
                List<Report> recentReports = allReports.stream()
                        .limit(5)
                        .collect(Collectors.toList());
                logger.info("Found {} reports, using {} recent ones", allReports.size(), recentReports.size());
                dashboardData.put("recentReports", recentReports);
            } catch (Exception e) {
                logger.error("Error fetching reports: {}", e.getMessage(), e);
                dashboardData.put("recentReports", new ArrayList<>());
            }
            
            // Milestone progress
            try {
                List<Milestone> completedMilestones = milestoneService.getCompletedMilestonesByStudentId(student.getId());
                List<Milestone> inProgressMilestones = milestoneService.getInProgressMilestonesByStudentId(student.getId());
                
                logger.info("Found {} completed and {} in-progress milestones", 
                          completedMilestones.size(), inProgressMilestones.size());
                
                dashboardData.put("completedMilestones", completedMilestones);
                dashboardData.put("inProgressMilestones", inProgressMilestones);
                
                // Calculate milestone completion rate safely
                double milestoneCompletionRate = 0.0;
                int totalMilestones = completedMilestones.size() + inProgressMilestones.size();
                if (totalMilestones > 0) {
                    milestoneCompletionRate = (double) completedMilestones.size() / totalMilestones * 100;
                }
                dashboardData.put("milestoneCompletionRate", milestoneCompletionRate);
            } catch (Exception e) {
                logger.error("Error fetching milestones: {}", e.getMessage(), e);
                dashboardData.put("completedMilestones", new ArrayList<>());
                dashboardData.put("inProgressMilestones", new ArrayList<>());
                dashboardData.put("milestoneCompletionRate", 0.0);
            }
            
            logger.info("Successfully prepared dashboard data");
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            logger.error("Error processing dashboard data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    @PutMapping("/milestone/{id}/complete")
    public ResponseEntity<Milestone> completeMilestone(@PathVariable Long id, Principal principal, 
                                                    @AuthenticationPrincipal OAuth2User oauth2User) {
        try {
            String email = null;
            
            // Handle different authentication types
            if (principal != null) {
                email = principal.getName();
            } else if (oauth2User != null) {
                email = oauth2User.getAttribute("email");
            }
            
            if (email == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Student student = studentService.getStudentByEmail(email);
            Milestone milestone = milestoneService.getMilestoneById(id);
            
            // Verify the milestone belongs to the current student
            if (!milestone.getStudent().getId().equals(student.getId())) {
                return ResponseEntity.badRequest().build();
            }
            
            // Use the completeMilestone method instead of updateMilestone
            milestone = milestoneService.completeMilestone(id);
            
            return ResponseEntity.ok(milestone);
        } catch (Exception e) {
            logger.error("Error completing milestone: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    @PutMapping("/report/{id}/notes")
    public ResponseEntity<Report> updateReportNotes(@PathVariable Long id, @RequestBody Map<String, String> payload,
                                                 Principal principal, @AuthenticationPrincipal OAuth2User oauth2User) {
        try {
            String email = null;
            
            // Handle different authentication types
            if (principal != null) {
                email = principal.getName();
            } else if (oauth2User != null) {
                email = oauth2User.getAttribute("email");
            }
            
            if (email == null || !payload.containsKey("notes")) {
                return ResponseEntity.badRequest().build();
            }
            
            Student student = studentService.getStudentByEmail(email);
            Report report = reportService.getReportById(id);
            
            // Verify the report belongs to the current student
            if (!report.getStudent().getId().equals(student.getId())) {
                return ResponseEntity.badRequest().build();
            }
            
            // Create a new report object with just the notes field set
            Report reportUpdate = new Report();
            reportUpdate.setNotes(payload.get("notes"));
            
            // Use the updateReport method with the correct signature
            report = reportService.updateReport(id, reportUpdate);
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error updating report notes: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
} 
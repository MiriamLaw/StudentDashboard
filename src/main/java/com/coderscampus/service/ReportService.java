package com.coderscampus.service;

import com.coderscampus.domain.Report;
import com.coderscampus.domain.Student;
import com.coderscampus.repository.ReportRepository;
import com.coderscampus.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    public List<Report> getReportsByStudentId(Long studentId) {
        if (studentId == null) {
            logger.error("Attempted to get reports with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        try {
            List<Report> reports = reportRepository.findByStudentIdOrderByReportDateDesc(studentId);
            logger.debug("Retrieved {} reports for student ID: {}", reports.size(), studentId);
            return reports;
        } catch (Exception e) {
            logger.error("Error retrieving reports for student ID: {}", studentId, e);
            return Collections.emptyList();
        }
    }
    
    public List<Report> getLatestReportsByStudentIds(List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            logger.error("Attempted to get latest reports with null or empty studentIds");
            throw new IllegalArgumentException("Student IDs cannot be null or empty");
        }
        
        try {
            // Since the repository doesn't have findLatestByStudentIds, we'll implement it manually
            logger.debug("Finding latest reports for {} students", studentIds.size());
            List<Report> latestReports = studentIds.stream()
                    .map(studentId -> {
                        List<Report> reports = reportRepository.findByStudentIdOrderByReportDateDesc(studentId);
                        return reports.isEmpty() ? null : reports.get(0);
                    })
                    .filter(report -> report != null)
                    .collect(Collectors.toList());
            
            logger.debug("Retrieved {} latest reports for {} students", latestReports.size(), studentIds.size());
            return latestReports;
        } catch (Exception e) {
            logger.error("Error retrieving latest reports for student IDs", e);
            return Collections.emptyList();
        }
    }
    
    public Report getReportById(Long id) {
        if (id == null) {
            logger.error("Attempted to get report with null id");
            throw new IllegalArgumentException("Report ID cannot be null");
        }
        
        try {
            return reportRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Report not found with id: {}", id);
                        return new IllegalArgumentException("Report not found with id: " + id);
                    });
        } catch (Exception e) {
            logger.error("Error retrieving report with ID: {}", id, e);
            throw e;
        }
    }
    
    public Report getReportByStudentIdAndDate(Long studentId, LocalDate reportDate) {
        if (studentId == null) {
            logger.error("Attempted to get report with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        if (reportDate == null) {
            logger.error("Attempted to get report with null reportDate for student ID: {}", studentId);
            throw new IllegalArgumentException("Report date cannot be null");
        }
        
        try {
            Optional<Report> reportOpt = reportRepository.findByStudentIdAndReportDate(studentId, reportDate);
            if (reportOpt.isPresent()) {
                logger.debug("Found report for student ID: {} and date: {}", studentId, reportDate);
                return reportOpt.get();
            } else {
                logger.debug("No report found for student ID: {} and date: {}", studentId, reportDate);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error retrieving report for student ID: {} and date: {}", studentId, reportDate, e);
            return null;
        }
    }
    
    @Transactional
    public Report createReport(Long studentId, Report report) {
        if (studentId == null) {
            logger.error("Attempted to create report with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        if (report == null) {
            logger.error("Attempted to create null report for student ID: {}", studentId);
            throw new IllegalArgumentException("Report cannot be null");
        }
        
        try {
            logger.debug("Creating new report for student ID: {}", studentId);
            
            // Check if report already exists for this date
            if (report.getReportDate() != null) {
                Optional<Report> existingReport = reportRepository.findByStudentIdAndReportDate(studentId, report.getReportDate());
                if (existingReport.isPresent()) {
                    logger.warn("Report already exists for student ID: {} and date: {}", studentId, report.getReportDate());
                    throw new IllegalStateException("Report already exists for this date");
                }
            } else {
                logger.debug("No report date provided, using current date");
                report.setReportDate(LocalDate.now());
            }
            
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> {
                        logger.warn("Student not found with id: {}", studentId);
                        return new IllegalArgumentException("Student not found with id: " + studentId);
                    });
            
            report.setStudent(student);
            
            Report savedReport = reportRepository.save(report);
            logger.info("Created new report with ID: {} for student ID: {}", savedReport.getId(), studentId);
            return savedReport;
        } catch (Exception e) {
            logger.error("Error creating report for student ID: {}", studentId, e);
            throw e;
        }
    }
    
    @Transactional
    public Report updateReport(Long id, Report reportDetails) {
        if (id == null) {
            logger.error("Attempted to update report with null id");
            throw new IllegalArgumentException("Report ID cannot be null");
        }
        
        if (reportDetails == null) {
            logger.error("Attempted to update report with null details for ID: {}", id);
            throw new IllegalArgumentException("Report details cannot be null");
        }
        
        try {
            logger.debug("Updating report with ID: {}", id);
            Report report = getReportById(id);
            
            // Update report fields
            if (reportDetails.getVimeoHoursSummary() != null) {
                report.setVimeoHoursSummary(reportDetails.getVimeoHoursSummary());
            }
            
            if (reportDetails.getAssignmentsSummary() != null) {
                report.setAssignmentsSummary(reportDetails.getAssignmentsSummary());
            }
            
            if (reportDetails.getEngagementScore() != null) {
                report.setEngagementScore(reportDetails.getEngagementScore());
            }
            
            if (reportDetails.getNotes() != null) {
                report.setNotes(reportDetails.getNotes());
            }
            
            Report updatedReport = reportRepository.save(report);
            logger.info("Updated report with ID: {}", id);
            return updatedReport;
        } catch (Exception e) {
            logger.error("Error updating report with ID: {}", id, e);
            throw e;
        }
    }
    
    @Transactional
    public void deleteReport(Long id) {
        if (id == null) {
            logger.error("Attempted to delete report with null id");
            throw new IllegalArgumentException("Report ID cannot be null");
        }
        
        try {
            // Check if report exists before deletion
            if (!reportRepository.existsById(id)) {
                logger.warn("Attempted to delete non-existent report with ID: {}", id);
                throw new IllegalArgumentException("Report not found with id: " + id);
            }
            
            logger.debug("Deleting report with ID: {}", id);
            reportRepository.deleteById(id);
            logger.info("Deleted report with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting report with ID: {}", id, e);
            throw e;
        }
    }
    
    public List<Report> getReportsByStudentIdAndDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        if (studentId == null) {
            logger.error("Attempted to get reports with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        if (startDate == null || endDate == null) {
            logger.error("Attempted to get reports with null date range for student ID: {}", studentId);
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            logger.error("Invalid date range: start date {} is after end date {} for student ID: {}", startDate, endDate, studentId);
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        try {
            List<Report> reports = reportRepository.findByStudentIdAndReportDateBetweenOrderByReportDateDesc(studentId, startDate, endDate);
            logger.debug("Retrieved {} reports for student ID: {} between {} and {}", reports.size(), studentId, startDate, endDate);
            return reports;
        } catch (Exception e) {
            logger.error("Error retrieving reports for student ID: {} between {} and {}", studentId, startDate, endDate, e);
            return Collections.emptyList();
        }
    }
    
    public List<Report> getWeeklyReports(Long studentId) {
        if (studentId == null) {
            logger.error("Attempted to get weekly reports with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            
            logger.debug("Retrieving weekly reports for student ID: {} from {} to {}", studentId, startOfWeek, endOfWeek);
            return getReportsByStudentIdAndDateRange(studentId, startOfWeek, endOfWeek);
        } catch (Exception e) {
            logger.error("Error retrieving weekly reports for student ID: {}", studentId, e);
            return Collections.emptyList();
        }
    }
    
    public boolean hasReportForToday(Long studentId) {
        if (studentId == null) {
            logger.error("Attempted to check for today's report with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        try {
            LocalDate today = LocalDate.now();
            Optional<Report> report = reportRepository.findByStudentIdAndReportDate(studentId, today);
            boolean hasReport = report.isPresent();
            logger.debug("Student ID: {} {} a report for today", studentId, hasReport ? "has" : "does not have");
            return hasReport;
        } catch (Exception e) {
            logger.error("Error checking for today's report for student ID: {}", studentId, e);
            return false;
        }
    }
    
    public double getAverageStudyHoursForWeek(Long studentId) {
        if (studentId == null) {
            logger.error("Attempted to get average study hours with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        try {
            List<Report> weeklyReports = getWeeklyReports(studentId);
            
            if (weeklyReports.isEmpty()) {
                logger.debug("No weekly reports found for student ID: {}, returning 0 average hours", studentId);
                return 0;
            }
            
            double totalHours = weeklyReports.stream()
                    .map(Report::getVimeoHoursSummary)
                    .filter(hours -> hours != null)
                    .mapToDouble(Double::doubleValue)
                    .sum();
            
            double averageHours = totalHours / weeklyReports.size();
            logger.debug("Average study hours for student ID: {} is {}", studentId, averageHours);
            return averageHours;
        } catch (Exception e) {
            logger.error("Error calculating average study hours for student ID: {}", studentId, e);
            return 0;
        }
    }
    
    public boolean isConsistentReporting(Long studentId) {
        if (studentId == null) {
            logger.error("Attempted to check reporting consistency with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate oneWeekAgo = today.minusDays(7);
            
            List<Report> recentReports = getReportsByStudentIdAndDateRange(studentId, oneWeekAgo, today);
            
            // Consider consistent if at least 5 reports in the last 7 days
            boolean isConsistent = recentReports.size() >= 5;
            logger.debug("Student ID: {} is {} in reporting (has {} reports in the last 7 days)", 
                    studentId, isConsistent ? "consistent" : "inconsistent", recentReports.size());
            return isConsistent;
        } catch (Exception e) {
            logger.error("Error checking reporting consistency for student ID: {}", studentId, e);
            return false;
        }
    }
}


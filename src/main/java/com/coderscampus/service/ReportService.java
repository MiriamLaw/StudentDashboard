package com.coderscampus.service;

import com.coderscampus.domain.Report;
import com.coderscampus.domain.Student;
import com.coderscampus.repository.ReportRepository;
import com.coderscampus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    public List<Report> getReportsByStudentId(Long studentId) {
        return reportRepository.findByStudentIdOrderByReportDateDesc(studentId);
    }
    
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));
    }
    
    @Transactional
    public Report createReport(Long studentId, Report report) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        
        // Check if report already exists for this date
        Optional<Report> existingReport = reportRepository.findByStudentIdAndReportDate(studentId, report.getReportDate());
        if (existingReport.isPresent()) {
            throw new IllegalArgumentException("A report already exists for this date");
        }
        
        report.setStudent(student);
        
        // Set default values if not provided
        if (report.getReportDate() == null) {
            report.setReportDate(LocalDate.now());
        }
        
        if (report.getEngagementScore() == null) {
            // Calculate engagement score based on assignments and video hours
            // Simple calculation: 0-100 scale
            int assignmentProgress = 0;
            if (student.getAssignmentsExpected() > 0) {
                assignmentProgress = (int) (((double) student.getAssignmentsSubmitted() / student.getAssignmentsExpected()) * 100);
            }
            
            report.setEngagementScore(Math.min(assignmentProgress, 100));
        }
        
        if (report.getVimeoHoursSummary() == null) {
            report.setVimeoHoursSummary(student.getVimeoHoursWatched());
        }
        
        return reportRepository.save(report);
    }
    
    @Transactional
    public Report updateReport(Long id, Report reportDetails) {
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
        
        return reportRepository.save(report);
    }
    
    @Transactional
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }
    
    @Transactional
    public Report generateWeeklyReport(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        
        LocalDate reportDate = LocalDate.now();
        
        // Check if report already exists for this date
        Optional<Report> existingReport = reportRepository.findByStudentIdAndReportDate(studentId, reportDate);
        if (existingReport.isPresent()) {
            return existingReport.get();
        }
        
        Report report = new Report();
        report.setStudent(student);
        report.setReportDate(reportDate);
        report.setVimeoHoursSummary(student.getVimeoHoursWatched());
        
        return reportRepository.save(report);
    }
}


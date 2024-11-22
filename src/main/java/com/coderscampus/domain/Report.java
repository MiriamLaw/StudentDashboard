package com.coderscampus.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private LocalDate reportDate;
    private String vimeoHoursSummary;
    private String assignmentSummary;
    private Double engagementScore;
    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getVimeoHoursSummary() {
        return vimeoHoursSummary;
    }

    public void setVimeoHoursSummary(String vimeoHoursSummary) {
        this.vimeoHoursSummary = vimeoHoursSummary;
    }

    public String getAssignmentSummary() {
        return assignmentSummary;
    }

    public void setAssignmentSummary(String assignmentSummary) {
        this.assignmentSummary = assignmentSummary;
    }

    public Double getEngagementScore() {
        return engagementScore;
    }

    public void setEngagementScore(Double engagementScore) {
        this.engagementScore = engagementScore;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

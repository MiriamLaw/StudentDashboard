package com.coderscampus.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private LocalDate reportDate;
    private Double vimeoHoursSummary;
    private String assignmentsSummary;
    private Integer engagementScore;
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

    public Double getVimeoHoursSummary() {
        return vimeoHoursSummary;
    }

    public void setVimeoHoursSummary(Double vimeoHoursSummary) {
        this.vimeoHoursSummary = vimeoHoursSummary;
    }

    public String getAssignmentsSummary() {
        return assignmentsSummary;
    }

    public void setAssignmentsSummary(String assignmentsSummary) {
        this.assignmentsSummary = assignmentsSummary;
    }

    public Integer getEngagementScore() {
        return engagementScore;
    }

    public void setEngagementScore(Integer engagementScore) {
        this.engagementScore = engagementScore;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

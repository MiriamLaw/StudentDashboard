package com.coderscampus.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "video_progress")
public class VideoProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private Integer weekNumber;
    private Double hoursWatched;

    public VideoProgress() {
    }

    public VideoProgress(Student student, Integer weekNumber, Double hoursWatched) {
        this.student = student;
        this.weekNumber = weekNumber;
        this.hoursWatched = hoursWatched;
    }

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

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public Double getHoursWatched() {
        return hoursWatched;
    }

    public void setHoursWatched(Double hoursWatched) {
        this.hoursWatched = hoursWatched;
    }
} 
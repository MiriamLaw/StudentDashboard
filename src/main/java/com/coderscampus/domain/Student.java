package com.coderscampus.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Entity
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private LocalDate startDate;
    private Integer weeksInBootcamp;

    private Integer assignmentsSubmitted;
    private Integer assignmentsExpected;

    private LocalDate lastAssignmentDate;
    private Double vimeoHoursWatched;

    private String role = "ROLE_USER";

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Milestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Report> reports = new ArrayList<>();

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL)
    private ProfileSettings profileSettings;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Integer getWeeksInBootcamp() {
        return weeksInBootcamp;
    }

    public void setWeeksInBootcamp(Integer weeksInBootcamp) {
        this.weeksInBootcamp = weeksInBootcamp;
    }

    public Integer getAssignmentsSubmitted() {
        return assignmentsSubmitted;
    }

    public void setAssignmentsSubmitted(Integer assignmentsSubmitted) {
        this.assignmentsSubmitted = assignmentsSubmitted;
    }

    public Integer getAssignmentsExpected() {
        return assignmentsExpected;
    }

    public void setAssignmentsExpected(Integer assignmentsExpected) {
        this.assignmentsExpected = assignmentsExpected;
    }

    public LocalDate getLastAssignmentDate() {
        return lastAssignmentDate;
    }

    public void setLastAssignmentDate(LocalDate lastAssignmentDate) {
        this.lastAssignmentDate = lastAssignmentDate;
    }

    public Double getVimeoHoursWatched() {
        return vimeoHoursWatched;
    }

    public void setVimeoHoursWatched(Double vimeoHoursWatched) {
        this.vimeoHoursWatched = vimeoHoursWatched;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public ProfileSettings getProfileSettings() {
        return profileSettings;
    }

    public void setProfileSettings(ProfileSettings profileSettings) {
        this.profileSettings = profileSettings;
    }

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<Milestone> milestones) {
        this.milestones = milestones;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }
}

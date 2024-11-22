package com.coderscampus.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
    public class Student {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @Column(unique = true)
        private String email;

        //startDate field! how?

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL)
    private Profile profile;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Milestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Report> reports = new ArrayList<>();
}

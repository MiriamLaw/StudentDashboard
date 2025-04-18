package com.coderscampus.config;

import com.coderscampus.domain.Student;
import com.coderscampus.repository.StudentRepository;
import com.coderscampus.service.DummyDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final DummyDataService dummyDataService;
    private final StudentRepository studentRepository;

    @Autowired
    public DataSeeder(DummyDataService dummyDataService, StudentRepository studentRepository) {
        this.dummyDataService = dummyDataService;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Starting DataSeeder...");
        
        // Seed demo students if none exist
        long studentCount = studentRepository.count();
        logger.info("Found {} existing students", studentCount);
        
        if (studentCount == 0) {
            logger.info("Creating demo students");
            dummyDataService.createDemoStudents();
        }
        
        // Add dummy data to any existing students that don't have it
        List<Student> allStudents = studentRepository.findAll();
        logger.info("Processing {} students for dummy data", allStudents.size());
        
        for (Student student : allStudents) {
            logger.info("Checking student: {}, ID: {}, isDemo: {}, has reports: {}", 
                      student.getName(), 
                      student.getId(),
                      student.getIsDemo(),
                      (student.getReports() != null && !student.getReports().isEmpty()));
            
            if (!student.getIsDemo() && (student.getReports() == null || student.getReports().isEmpty())) {
                logger.info("Adding dummy data to student: {}", student.getName());
                dummyDataService.addDummyDataToExistingStudent(student);
            }
        }
        
        logger.info("DataSeeder complete");
    }
} 
package com.coderscampus.config;

import com.coderscampus.domain.Student;
import com.coderscampus.repository.StudentRepository;
import com.coderscampus.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private StudentService studentService;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${admin.emails}")
    private String[] adminEmails;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Create admin users
        createAdminUsers();
        
        // Create sample data
        studentService.createSampleData();
    }
    
    private void createAdminUsers() {
        Arrays.stream(adminEmails).forEach(email -> {
            // Check if admin user already exists
            if (!studentRepository.existsByEmail(email)) {
                Student adminUser = new Student();
                adminUser.setName("Admin User");
                adminUser.setEmail(email);
                adminUser.setPassword(passwordEncoder.encode("adminpassword"));
                adminUser.setRole("ROLE_ADMIN");
                adminUser.setStartDate(LocalDate.now().minusMonths(6));
                adminUser.setWeeksInBootcamp(24);
                adminUser.setAssignmentsSubmitted(48);
                adminUser.setAssignmentsExpected(48);
                adminUser.setVimeoHoursWatched(120.0);
                adminUser.setLastAssignmentDate(LocalDate.now().minusDays(2));
                
                studentRepository.save(adminUser);
                
                System.out.println("Created admin user with email: " + email);
            }
        });
    }
} 
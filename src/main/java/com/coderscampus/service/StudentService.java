package com.coderscampus.service;

import com.coderscampus.domain.Milestone;
import com.coderscampus.domain.ProfileSettings;
import com.coderscampus.domain.Student;
import com.coderscampus.repository.MilestoneRepository;
import com.coderscampus.repository.ProfileSettingsRepository;
import com.coderscampus.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProfileSettingsRepository profileSettingsRepository;
    
    @Autowired
    private MilestoneRepository milestoneRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private DummyDataService dummyDataService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to load user with username/email: {}", username);
        
        try {
            // Try to find by email
            Student student = studentRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
            
            logger.info("Successfully loaded user: {}", student.getEmail());
            
            return new User(
                student.getEmail(),
                student.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(student.getRole()))
            );
        } catch (Exception e) {
            logger.error("Error loading user: {}", e.getMessage());
            throw e;
        }
    }
    
    @Transactional
    public Student registerNewStudent(Student student) {
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        
        // Encode password
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        
        // Set default values
        student.setStartDate(LocalDate.now());
        student.setWeeksInBootcamp(0);
        student.setAssignmentsSubmitted(0);
        student.setAssignmentsExpected(0);
        student.setVimeoHoursWatched(0.0);
        
        // Save student
        Student savedStudent = studentRepository.save(student);
        
        // Create default profile settings
        ProfileSettings profileSettings = new ProfileSettings();
        profileSettings.setStudent(savedStudent);
        profileSettingsRepository.save(profileSettings);
        
        // Add dummy data for the new student
        dummyDataService.addDummyDataToExistingStudent(savedStudent);
        
        return savedStudent;
    }
    
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));
    }
    
    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with email: " + email));
    }
    
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
    
    @Transactional
    public Student updateStudent(Long id, Student studentDetails) {
        Student student = getStudentById(id);
        
        // Update student fields
        student.setName(studentDetails.getName());
        // Don't update password here - create a separate method for that
        
        // Update bootcamp metrics if provided
        if (studentDetails.getAssignmentsSubmitted() != null) {
            student.setAssignmentsSubmitted(studentDetails.getAssignmentsSubmitted());
        }
        
        if (studentDetails.getAssignmentsExpected() != null) {
            student.setAssignmentsExpected(studentDetails.getAssignmentsExpected());
        }
        
        if (studentDetails.getVimeoHoursWatched() != null) {
            student.setVimeoHoursWatched(studentDetails.getVimeoHoursWatched());
        }
        
        if (studentDetails.getLastAssignmentDate() != null) {
            student.setLastAssignmentDate(studentDetails.getLastAssignmentDate());
        }
        
        // Calculate weeks in bootcamp
        if (student.getStartDate() != null) {
            long weeks = ChronoUnit.WEEKS.between(student.getStartDate(), LocalDate.now());
            student.setWeeksInBootcamp((int) weeks);
        }
        
        return studentRepository.save(student);
    }
    
    @Transactional
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
    
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        Student student = getStudentById(id);
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, student.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Set new password
        student.setPassword(passwordEncoder.encode(newPassword));
        studentRepository.save(student);
    }
    
    @Transactional
    public void createSampleData() {
        // Check if we already have students
        if (studentRepository.count() > 0) {
            return; // Don't add sample data if we already have students
        }
        
        // Create 5 sample students
        for (int i = 1; i <= 5; i++) {
            Student student = new Student();
            student.setName("Student " + i);
            student.setEmail("student" + i + "@example.com");
            student.setPassword(passwordEncoder.encode("password"));
            student.setStartDate(LocalDate.now().minusWeeks(i * 2));
            student.setWeeksInBootcamp(i * 2);
            student.setAssignmentsSubmitted(5 * i);
            student.setAssignmentsExpected(7 * i);
            student.setVimeoHoursWatched(10.0 * i);
            student.setLastAssignmentDate(LocalDate.now().minusDays(i));
            
            Student savedStudent = studentRepository.save(student);
            
            // Create profile settings for the student
            ProfileSettings profileSettings = new ProfileSettings();
            profileSettings.setStudent(savedStudent);
            profileSettings.setNotificationsEnabled(true);
            profileSettings.setThemePreference("light");
            profileSettingsRepository.save(profileSettings);
            
            // Create some milestones
            for (int j = 1; j <= 3; j++) {
                Milestone milestone = new Milestone();
                milestone.setStudent(savedStudent);
                milestone.setMilestoneName("Milestone " + j + " for Student " + i);
                milestone.setStatus(j < 2 ? Milestone.MilestoneStatus.COMPLETED : Milestone.MilestoneStatus.IN_PROGRESS);
                if (j < 2) {
                    milestone.setCompletionDate(LocalDate.now().minusDays(j * 5));
                }
                milestone.setComment("This is a sample milestone");
                milestoneRepository.save(milestone);
            }
        }
    }
}

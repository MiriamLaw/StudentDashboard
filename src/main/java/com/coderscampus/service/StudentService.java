package com.coderscampus.service;

import com.coderscampus.domain.Milestone;
import com.coderscampus.domain.ProfileSettings;
import com.coderscampus.domain.Student;
import com.coderscampus.repository.ProfileSettingsRepository;
import com.coderscampus.repository.StudentRepository;
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

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private ProfileSettingsRepository profileSettingsRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return new User(
            student.getEmail(),
            student.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(student.getRole()))
        );
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
}

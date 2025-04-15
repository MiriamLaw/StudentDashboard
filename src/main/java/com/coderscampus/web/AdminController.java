package com.coderscampus.web;

import com.coderscampus.domain.Student;
import com.coderscampus.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private StudentService studentService;
    
    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/students/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        List<Student> students = studentService.getAllStudents();
        
        // Calculate statistics
        int totalStudents = students.size();
        
        long assignmentsSubmitted = 0;
        long assignmentsExpected = 0;
        double totalVimeoHours = 0;
        
        for (Student student : students) {
            assignmentsSubmitted += student.getAssignmentsSubmitted() != null ? student.getAssignmentsSubmitted() : 0;
            assignmentsExpected += student.getAssignmentsExpected() != null ? student.getAssignmentsExpected() : 0;
            totalVimeoHours += student.getVimeoHoursWatched() != null ? student.getVimeoHoursWatched() : 0;
        }
        
        double averageAssignmentsSubmitted = totalStudents > 0 ? (double) assignmentsSubmitted / totalStudents : 0;
        double averageVimeoHours = totalStudents > 0 ? totalVimeoHours / totalStudents : 0;
        double assignmentCompletionRate = assignmentsExpected > 0 ? (double) assignmentsSubmitted / assignmentsExpected * 100 : 0;
        
        // Add statistics to dashboard data
        dashboardData.put("totalStudents", totalStudents);
        dashboardData.put("totalAssignmentsSubmitted", assignmentsSubmitted);
        dashboardData.put("totalAssignmentsExpected", assignmentsExpected);
        dashboardData.put("totalVimeoHours", totalVimeoHours);
        dashboardData.put("averageAssignmentsSubmitted", averageAssignmentsSubmitted);
        dashboardData.put("averageVimeoHours", averageVimeoHours);
        dashboardData.put("assignmentCompletionRate", assignmentCompletionRate);
        
        return ResponseEntity.ok(dashboardData);
    }
} 
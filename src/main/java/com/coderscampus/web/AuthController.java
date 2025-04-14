package com.coderscampus.web;

import com.coderscampus.domain.Student;
import com.coderscampus.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private StudentService studentService;
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Student student) {
        try {
            Student registeredStudent = studentService.registerNewStudent(student);
            return ResponseEntity.ok(registeredStudent);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Login is handled by Spring Security
    
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(@RequestParam String email) {
        try {
            Student student = studentService.getStudentByEmail(email);
            return ResponseEntity.ok(student);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
} 
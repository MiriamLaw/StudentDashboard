package com.coderscampus.web;

import com.coderscampus.domain.Student;
import com.coderscampus.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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
    
    // Form login is handled by Spring Security
    
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                throw new IllegalArgumentException("No authenticated user found");
            }
            
            String email;
            
            // Handle different authentication types
            if (authentication instanceof OAuth2AuthenticationToken) {
                // OAuth2 authentication
                OAuth2User oauth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
                email = oauth2User.getAttribute("email");
            } else {
                // Form authentication
                email = authentication.getName();
            }
            
            if (email == null) {
                throw new IllegalArgumentException("Unable to determine user email");
            }
            
            Student student = studentService.getStudentByEmail(email);
            return ResponseEntity.ok(student);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @GetMapping("/oauth2-success")
    public ResponseEntity<?> handleOAuth2Success(Authentication authentication) {
        try {
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2User oauth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
                String email = oauth2User.getAttribute("email");
                
                if (email != null) {
                    // Get the user by email
                    Student student = studentService.getStudentByEmail(email);
                    return ResponseEntity.ok(student);
                }
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("error", "OAuth2 authentication failed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Get authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null) {
                // Perform logout
                new SecurityContextLogoutHandler().logout(request, response, authentication);
            }
            
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Logout successful");
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Logout failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }
} 
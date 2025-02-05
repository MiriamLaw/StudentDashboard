package com.coderscampus.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coderscampus.domain.Student;
import com.coderscampus.service.StudentService;

import java.time.LocalDate;

@RestController
public class HomeController {

    @Autowired
    private StudentService studentService; // You'll need this to work with student data

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Home");
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        // The @AuthenticationPrincipal OAuth2User principal gives us access to the logged-in user's info
        
        // Extract email from OAuth2User
        String email = principal.getAttribute("email");
        
        // Add basic user info to model
        model.addAttribute("pageTitle", "Student Dashboard");
        
        // Create some test student data
        Student testStudent = new Student();
        testStudent.setEmail(email);
        // testStudent.setStartDate(LocalDate.now().minusWeeks(10)); // Example: started 10 weeks ago
        // testStudent.setWeeksInProgram(10L); // Example weeks in program
        
        // Add the test student to the model
        model.addAttribute("student", testStudent);
        
        // You can also add other test attributes
        model.addAttribute("currentDate", LocalDate.now());
        
        return "dashboard";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        // The admin page is restricted to users with ROLE_ADMIN.
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin";
    }

}
package com.coderscampus.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;

import com.coderscampus.domain.Student;
import com.coderscampus.service.StudentService;

import java.time.LocalDate;

@Controller
public class HomeController {

    @Autowired
    private StudentService studentService; // You'll need this to work with student data

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OAuth2User principal) {
        // This will trigger OAuth2 at root endpoint
        return "index";  // Return index page with login links
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // If student is not in model, add it
        if (!model.containsAttribute("student")) {
            model.addAttribute("student", new Student());
        }
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@ModelAttribute Student student, 
                              RedirectAttributes redirectAttributes,
                              Model model) {
        // Check if passwords match
        if (student.getConfirmPassword() != null && 
            !student.getConfirmPassword().equals(student.getPassword())) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }
        
        try {
            Student registeredStudent = studentService.registerNewStudent(student);
            if (registeredStudent != null) {
                redirectAttributes.addFlashAttribute("message", 
                    "Registration successful! Please login with your email and password.");
                return "redirect:/login";
            } else {
                model.addAttribute("error", "Registration failed. Please try again.");
                return "register";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal Object principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        String email = null;
        String name = null;
        
        // Handle OAuth2 principal
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
        } 
        // Handle UserDetails principal from form login
        else if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            email = userDetails.getUsername(); // This is the email
            
            try {
                // Get the full student object to access the name
                Student student = studentService.getStudentByEmail(email);
                name = student.getName();
            } catch (Exception e) {
                // Just use email if we can't get the name
                name = email;
            }
        }
        
        model.addAttribute("userEmail", email);
        model.addAttribute("userName", name);
        model.addAttribute("pageTitle", "Student Dashboard");
        
        return "dashboard";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String studentDetail(@PathVariable Long id, Model model) {
        model.addAttribute("studentId", id);
        return "admin/student-detail";
    }

}
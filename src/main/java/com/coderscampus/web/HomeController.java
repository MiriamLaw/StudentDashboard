package com.coderscampus.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String dashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        String email = principal.getAttribute("email");
        model.addAttribute("userEmail", email);
        model.addAttribute("pageTitle", "Student Dashboard");
        
        return "dashboard";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        // The admin page is restricted to users with ROLE_ADMIN.
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
package com.coderscampus.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

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
        return "dashboard";  // Go straight to dashboard after authentication
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "redirect:/";  // Redirect to login if not authenticated
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

}
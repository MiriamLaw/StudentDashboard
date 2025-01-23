package com.coderscampus.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")  // Base path for all student-related endpoints
public class StudentController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Home");
        return "index";
    }

    @GetMapping("/dashboard")
    public String secured(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        return "dashboard";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        // The admin page is restricted to users with ROLE_ADMIN.
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        model.addAttribute("pageTitle", "Student Profile");
        return "profile";
    }

    @GetMapping("/milestones")
    public String viewMilestones(Model model) {
        model.addAttribute("pageTitle", "My Milestones");
        return "milestones";
    }

    @GetMapping("/reports")
    public String viewReports(Model model) {
        model.addAttribute("pageTitle", "My Reports");
        return "reports";
    }

    // Optional: Add an endpoint for viewing progress/statistics
    @GetMapping("/progress")
    public String viewProgress(Model model) {
        model.addAttribute("pageTitle", "My Progress");
        return "progress";
    }
}
package com.coderscampus.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

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

}
package com.coderscampus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/").permitAll();
                    auth.requestMatchers("/h2-console/**").permitAll(); // Allow access to H2 console
                    auth.requestMatchers("/admin/**").hasRole("ADMIN"); // Only admins can access /admin/**
                    auth.requestMatchers("/user/**").hasRole("USER");   // Only users can access /user/**
                    auth.anyRequest().authenticated();
                })
//                .oauth2Login(withDefaults()) // Use this line instead of 22-23 if prefer to manually enter the endpoint for /secured.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**")) // Disable CSRF protection for H2 console
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())) // Disable frame options using new API
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/dashboard", true)) // Redirects to /secured after login. Can be set to /dashboard or other.
                .formLogin(withDefaults())
                .build();
    }
}














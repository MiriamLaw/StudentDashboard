package com.coderscampus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/").permitAll();
                    auth.requestMatchers("/register").permitAll(); // Allow access to registration
                    auth.requestMatchers("/api/auth/**").permitAll(); // Allow access to auth endpoints
                    auth.requestMatchers("/h2-console/**").permitAll(); // Allow access to H2 console
                    auth.requestMatchers("/admin/**").hasRole("ADMIN"); // Only admins can access /admin/**
                    auth.requestMatchers("/user/**").hasRole("USER");   // Only users can access /user/**
                    auth.anyRequest().authenticated();
                })
                .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/h2-console/**")
                    .ignoringRequestMatchers("/api/**")) // Disable CSRF for API endpoints
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())) // Disable frame options for H2 console
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/dashboard", true))
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/dashboard", true))
                .logout(logout -> logout
                        .permitAll()
                        .logoutSuccessUrl("/login?logout"))
                .build();
    }
}














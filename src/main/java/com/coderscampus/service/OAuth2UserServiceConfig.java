package com.coderscampus.service;

import com.coderscampus.domain.ProfileSettings;
import com.coderscampus.domain.Student;
import com.coderscampus.repository.ProfileSettingsRepository;
import com.coderscampus.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


// Uncomment the OAuth2UserService configuration below if you want to customize roles or map additional user data.
// This configuration is optional and only needed if you wish to assign custom roles (e.g., ROLE_ADMIN) or handle extra user attributes.
// NOTE: If you uncomment and use this configuration, remember to add `.userInfoEndpoint().userService(oauth2UserService())`
// in the `SecurityConfig` to connect this custom service to your OAuth2 login.

@Configuration
public class OAuth2UserServiceConfig {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserServiceConfig.class);

    @Value("${admin.emails}")
    private String[] adminEmails;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private ProfileSettingsRepository profileSettingsRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        return userRequest -> {
            // Load user information from the OAuth2 provider
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            Map<String, Object> attributes = oAuth2User.getAttributes();
            
            // Get user's email and name
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            
            logger.info("OAuth2 login attempt - Email: {}, Name: {}", email, name);
            logger.info("Admin emails configured: {}", Arrays.toString(adminEmails));
            logger.info("Is admin email? {}", Arrays.asList(adminEmails).contains(email));
            
            // First, directly check if email is in admin list
            boolean isAdminEmail = email != null && Arrays.asList(adminEmails).contains(email);
            
            // Store user in database
            Student student = null;
            if (email != null && name != null) {
                student = processOAuth2User(email, name, isAdminEmail);
                logger.info("Student record from DB - Email: {}, Role: {}", student.getEmail(), student.getRole());
            }
            
            // Build authorities based on admin check AND database role
            Set<SimpleGrantedAuthority> authorities = new HashSet<>();
            
            // Always check if this is an admin email - this is our source of truth
            if (isAdminEmail) {
                logger.info("Adding ROLE_ADMIN authority based on admin email list");
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                logger.info("Adding ROLE_USER authority (not in admin email list)");
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            
            // Create new OAuth2User with our authorities
            DefaultOAuth2User customOAuth2User = new DefaultOAuth2User(
                authorities, 
                attributes, 
                "email" // Using email as the name attribute key
            );
            
            logger.info("Created OAuth2User with authorities: {}", authorities);
            return customOAuth2User;
        };
    }
    
    @Transactional
    private Student processOAuth2User(String email, String name, boolean isAdmin) {
        // Check if user already exists
        Optional<Student> existingStudent = studentRepository.findByEmail(email);
        
        if (existingStudent.isPresent()) {
            Student student = existingStudent.get();
            logger.info("Found existing student with email: {} and role: {}", email, student.getRole());
            
            // Always update role based on admin email list
            if (isAdmin && !student.getRole().equals("ROLE_ADMIN")) {
                logger.info("Updating role to ROLE_ADMIN for user: {}", email);
                student.setRole("ROLE_ADMIN");
                studentRepository.save(student);
            } else if (!isAdmin && !student.getRole().equals("ROLE_USER")) {
                logger.info("Updating role to ROLE_USER for user: {}", email);
                student.setRole("ROLE_USER");
                studentRepository.save(student);
            }
            
            return student;
        } else {
            logger.info("Creating new student with email: {}", email);
            // Create new user
            Student newStudent = new Student();
            newStudent.setEmail(email);
            newStudent.setName(name);
            newStudent.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            
            // Set role based on admin check
            if (isAdmin) {
                logger.info("Setting role to ROLE_ADMIN for new user: {}", email);
                newStudent.setRole("ROLE_ADMIN");
            } else {
                logger.info("Setting role to ROLE_USER for new user: {}", email);
                newStudent.setRole("ROLE_USER");
            }
            
            // Set default values
            newStudent.setStartDate(LocalDate.now());
            newStudent.setWeeksInBootcamp(0);
            newStudent.setAssignmentsSubmitted(0);
            newStudent.setAssignmentsExpected(0);
            newStudent.setVimeoHoursWatched(0.0);
            
            // Save student
            Student savedStudent = studentRepository.save(newStudent);
            logger.info("Saved new student with ID: {} and role: {}", savedStudent.getId(), savedStudent.getRole());
            
            // Create profile settings
            ProfileSettings profileSettings = new ProfileSettings();
            profileSettings.setStudent(savedStudent);
            profileSettingsRepository.save(profileSettings);
            
            return savedStudent;
        }
    }
}



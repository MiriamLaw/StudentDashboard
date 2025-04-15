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
            OAuth2User oauth2User = delegate.loadUser(userRequest);
            Map<String, Object> attributes = oauth2User.getAttributes();
            
            // Extract email and name from attributes
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            
            logger.info("OAuth2 login attempt - Email: {}, Name: {}", email, name);
            logger.info("Admin emails configured: {}", Arrays.toString(adminEmails));
            
            // Check if this is an admin email
            boolean isAdminEmail = email != null && Arrays.asList(adminEmails).contains(email);
            logger.info("Is admin email? {}", isAdminEmail);
            
            // Save/update the user in the database
            if (email != null && name != null) {
                processOAuth2User(email, name, isAdminEmail);
            }
            
            // Create authorities set
            Set<SimpleGrantedAuthority> authorities = new HashSet<>();
            
            // Always use the admin email list as the source of truth
            if (isAdminEmail) {
                logger.info("Adding ROLE_ADMIN authority based on admin email list");
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                logger.info("Adding ROLE_USER authority (not in admin email list)");
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            
            // Ensure email is in the attributes map for proper DefaultOAuth2User creation
            if (email != null && !attributes.containsKey("email")) {
                attributes.put("email", email);
            }
            
            // Google OAuth2 uses "sub" as the nameAttributeKey
            return new DefaultOAuth2User(authorities, attributes, "sub");
        };
    }
    
    @Transactional
    private Student processOAuth2User(String email, String name, boolean isAdmin) {
        // Check if user already exists
        Optional<Student> existingStudent = studentRepository.findByEmail(email);
        
        if (existingStudent.isPresent()) {
            Student student = existingStudent.get();
            logger.info("Found existing student with email: {} and role: {}", email, student.getRole());
            
            // Update role if needed
            String expectedRole = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
            if (!student.getRole().equals(expectedRole)) {
                logger.info("Updating role to {} for user: {}", expectedRole, email);
                student.setRole(expectedRole);
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



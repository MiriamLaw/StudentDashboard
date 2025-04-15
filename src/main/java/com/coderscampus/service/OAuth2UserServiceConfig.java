package com.coderscampus.service;

import com.coderscampus.domain.ProfileSettings;
import com.coderscampus.domain.Student;
import com.coderscampus.repository.ProfileSettingsRepository;
import com.coderscampus.repository.StudentRepository;
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
            // Load user information from the OAuth2 provider (Google in this example)
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            Map<String, Object> attributes = oAuth2User.getAttributes();
            
            // Get user's email and name from the OAuth2 provider's attributes
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            
            // Initialize the set of authorities
            Set<SimpleGrantedAuthority> authorities = new HashSet<>();

            // Store/update the user in our database
            if (email != null && name != null) {
                Student student = processOAuth2User(email, name);
                
                // Assign roles based on the user's role in our database
                if (Arrays.asList(adminEmails).contains(email)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                } else {
                    authorities.add(new SimpleGrantedAuthority(student.getRole()));
                }
            } else {
                // Default role if we can't determine the user
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            // Return a new user with the assigned roles and Google's identifier ("sub")
            return new DefaultOAuth2User(authorities, attributes, "sub");
        };
    }
    
    @Transactional
    private Student processOAuth2User(String email, String name) {
        // Check if user already exists
        Optional<Student> existingStudent = studentRepository.findByEmail(email);
        
        if (existingStudent.isPresent()) {
            // User exists, return the existing user
            return existingStudent.get();
        } else {
            // Create a new user from OAuth2 data
            Student newStudent = new Student();
            newStudent.setEmail(email);
            newStudent.setName(name);
            // OAuth2 users don't have a password in our database since they authenticate through the provider
            newStudent.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            
            // Set default values
            newStudent.setStartDate(LocalDate.now());
            newStudent.setWeeksInBootcamp(0);
            newStudent.setAssignmentsSubmitted(0);
            newStudent.setAssignmentsExpected(0);
            newStudent.setVimeoHoursWatched(0.0);
            
            // Save student
            Student savedStudent = studentRepository.save(newStudent);
            
            // Create default profile settings
            ProfileSettings profileSettings = new ProfileSettings();
            profileSettings.setStudent(savedStudent);
            profileSettingsRepository.save(profileSettings);
            
            return savedStudent;
        }
    }
}



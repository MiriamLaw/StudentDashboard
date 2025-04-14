package com.coderscampus.service;

import com.coderscampus.domain.ProfileSettings;
import com.coderscampus.domain.Student;
import com.coderscampus.repository.ProfileSettingsRepository;
import com.coderscampus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileSettingsService {

    @Autowired
    private ProfileSettingsRepository profileSettingsRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    public ProfileSettings getProfileSettingsByStudentId(Long studentId) {
        return profileSettingsRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Profile settings not found for student id: " + studentId));
    }
    
    @Transactional
    public ProfileSettings updateProfileSettings(Long studentId, ProfileSettings profileDetails) {
        ProfileSettings profileSettings = getProfileSettingsByStudentId(studentId);
        
        // Update settings
        profileSettings.setNotificationsEnabled(profileDetails.getNotificationsEnabled());
        profileSettings.setThemePreference(profileDetails.getThemePreference());
        profileSettings.setBio(profileDetails.getBio());
        profileSettings.setGithubUrl(profileDetails.getGithubUrl());
        profileSettings.setLinkedinUrl(profileDetails.getLinkedinUrl());
        
        return profileSettingsRepository.save(profileSettings);
    }
    
    @Transactional
    public ProfileSettings createDefaultProfileSettings(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        
        ProfileSettings profileSettings = new ProfileSettings();
        profileSettings.setStudent(student);
        profileSettings.setNotificationsEnabled(true);
        profileSettings.setThemePreference("default");
        
        return profileSettingsRepository.save(profileSettings);
    }
}

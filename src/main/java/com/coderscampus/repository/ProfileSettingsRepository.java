package com.coderscampus.repository;

import com.coderscampus.domain.ProfileSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileSettingsRepository extends JpaRepository<ProfileSettings, Long> {
    Optional<ProfileSettings> findByStudentId(Long studentId);
}

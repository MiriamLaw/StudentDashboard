package com.coderscampus.repository;

import com.coderscampus.domain.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoProgressRepository extends JpaRepository<VideoProgress, Long> {
    List<VideoProgress> findByStudentIdOrderByWeekNumberAsc(Long studentId);
    List<VideoProgress> findByStudentIdAndWeekNumberLessThanEqual(Long studentId, Integer weekNumber);
} 
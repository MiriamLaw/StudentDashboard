package com.coderscampus.repository;

import com.coderscampus.domain.Milestone;
import com.coderscampus.domain.Milestone.MilestoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByStudentId(Long studentId);
    List<Milestone> findByStudentIdAndStatus(Long studentId, MilestoneStatus status);
    long countByStudentIdAndStatus(Long studentId, MilestoneStatus status);
}

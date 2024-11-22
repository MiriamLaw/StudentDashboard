package com.coderscampus.repository;

import com.coderscampus.domain.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByStatus(Milestone.Status status);

}

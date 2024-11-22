package com.coderscampus.service;

import com.coderscampus.domain.Milestone;
import com.coderscampus.repository.MilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MilestoneService {

    @Autowired
    private MilestoneRepository milestoneRepository;

    public List<Milestone> getMilestonesInProgress() {
        return milestoneRepository.findByStatus(Milestone.Status.IN_PROGRESS);
    }
}

package com.coderscampus.service;

import com.coderscampus.domain.Milestone;
import com.coderscampus.domain.Milestone.MilestoneStatus;
import com.coderscampus.domain.Student;
import com.coderscampus.repository.MilestoneRepository;
import com.coderscampus.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class MilestoneService {
    
    private static final Logger logger = LoggerFactory.getLogger(MilestoneService.class);

    @Autowired
    private MilestoneRepository milestoneRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    public List<Milestone> getMilestonesByStudentId(Long studentId) {
        if (studentId == null) {
            logger.error("Attempted to get milestones with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        try {
            List<Milestone> milestones = milestoneRepository.findByStudentId(studentId);
            logger.debug("Retrieved {} milestones for student ID: {}", milestones.size(), studentId);
            return milestones;
        } catch (Exception e) {
            logger.error("Error retrieving milestones for student ID: {}", studentId, e);
            return Collections.emptyList();
        }
    }
    
    public List<Milestone> getCompletedMilestonesByStudentId(Long studentId) {
        if (studentId == null) {
            logger.error("Attempted to get completed milestones with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        try {
            List<Milestone> milestones = milestoneRepository.findByStudentIdAndStatus(studentId, MilestoneStatus.COMPLETED);
            logger.debug("Retrieved {} completed milestones for student ID: {}", milestones.size(), studentId);
            return milestones;
        } catch (Exception e) {
            logger.error("Error retrieving completed milestones for student ID: {}", studentId, e);
            return Collections.emptyList();
        }
    }
    
    public List<Milestone> getInProgressMilestonesByStudentId(Long studentId) {
        if (studentId == null) {
            logger.error("Attempted to get in-progress milestones with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        try {
            List<Milestone> milestones = milestoneRepository.findByStudentIdAndStatus(studentId, MilestoneStatus.IN_PROGRESS);
            logger.debug("Retrieved {} in-progress milestones for student ID: {}", milestones.size(), studentId);
            return milestones;
        } catch (Exception e) {
            logger.error("Error retrieving in-progress milestones for student ID: {}", studentId, e);
            return Collections.emptyList();
        }
    }
    
    public Milestone getMilestoneById(Long id) {
        if (id == null) {
            logger.error("Attempted to get milestone with null id");
            throw new IllegalArgumentException("Milestone ID cannot be null");
        }
        
        try {
            return milestoneRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Milestone not found with id: {}", id);
                        return new IllegalArgumentException("Milestone not found with id: " + id);
                    });
        } catch (Exception e) {
            logger.error("Error retrieving milestone with ID: {}", id, e);
            throw e;
        }
    }
    
    @Transactional
    public Milestone createMilestone(Long studentId, Milestone milestone) {
        if (studentId == null) {
            logger.error("Attempted to create milestone with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        if (milestone == null) {
            logger.error("Attempted to create null milestone for student ID: {}", studentId);
            throw new IllegalArgumentException("Milestone cannot be null");
        }
        
        try {
            logger.debug("Creating new milestone for student ID: {}", studentId);
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> {
                        logger.warn("Student not found with id: {}", studentId);
                        return new IllegalArgumentException("Student not found with id: " + studentId);
                    });
            
            milestone.setStudent(student);
            
            // Set default values
            if (milestone.getStatus() == null) {
                milestone.setStatus(MilestoneStatus.IN_PROGRESS);
                logger.debug("Setting default status to IN_PROGRESS for new milestone");
            }
            
            Milestone savedMilestone = milestoneRepository.save(milestone);
            logger.info("Created new milestone with ID: {} for student ID: {}", savedMilestone.getId(), studentId);
            return savedMilestone;
        } catch (Exception e) {
            logger.error("Error creating milestone for student ID: {}", studentId, e);
            throw e;
        }
    }
    
    @Transactional
    public Milestone updateMilestone(Long id, Milestone milestoneDetails) {
        if (id == null) {
            logger.error("Attempted to update milestone with null id");
            throw new IllegalArgumentException("Milestone ID cannot be null");
        }
        
        if (milestoneDetails == null) {
            logger.error("Attempted to update milestone with null details for ID: {}", id);
            throw new IllegalArgumentException("Milestone details cannot be null");
        }
        
        try {
            logger.debug("Updating milestone with ID: {}", id);
            Milestone milestone = getMilestoneById(id);
            
            // Update milestone fields
            milestone.setMilestoneName(milestoneDetails.getMilestoneName());
            milestone.setStatus(milestoneDetails.getStatus());
            milestone.setComment(milestoneDetails.getComment());
            
            // If status is changed to completed, set completion date
            if (milestone.getStatus() == MilestoneStatus.COMPLETED && milestone.getCompletionDate() == null) {
                milestone.setCompletionDate(LocalDate.now());
                logger.debug("Setting completion date to today for milestone ID: {}", id);
            }
            
            Milestone updatedMilestone = milestoneRepository.save(milestone);
            logger.info("Updated milestone with ID: {}", id);
            return updatedMilestone;
        } catch (Exception e) {
            logger.error("Error updating milestone with ID: {}", id, e);
            throw e;
        }
    }
    
    @Transactional
    public void deleteMilestone(Long id) {
        if (id == null) {
            logger.error("Attempted to delete milestone with null id");
            throw new IllegalArgumentException("Milestone ID cannot be null");
        }
        
        try {
            // Check if milestone exists before deletion
            if (!milestoneRepository.existsById(id)) {
                logger.warn("Attempted to delete non-existent milestone with ID: {}", id);
                throw new IllegalArgumentException("Milestone not found with id: " + id);
            }
            
            logger.debug("Deleting milestone with ID: {}", id);
            milestoneRepository.deleteById(id);
            logger.info("Deleted milestone with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting milestone with ID: {}", id, e);
            throw e;
        }
    }
    
    @Transactional
    public Milestone completeMilestone(Long id) {
        if (id == null) {
            logger.error("Attempted to complete milestone with null id");
            throw new IllegalArgumentException("Milestone ID cannot be null");
        }
        
        try {
            logger.debug("Marking milestone as completed for ID: {}", id);
            Milestone milestone = getMilestoneById(id);
            milestone.setStatus(MilestoneStatus.COMPLETED);
            milestone.setCompletionDate(LocalDate.now());
            
            Milestone completedMilestone = milestoneRepository.save(milestone);
            logger.info("Marked milestone as completed with ID: {}", id);
            return completedMilestone;
        } catch (Exception e) {
            logger.error("Error completing milestone with ID: {}", id, e);
            throw e;
        }
    }
    
    public long getCompletedMilestonesCount(Long studentId) {
        if (studentId == null) {
            logger.error("Attempted to get completed milestones count with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        try {
            long count = milestoneRepository.countByStudentIdAndStatus(studentId, MilestoneStatus.COMPLETED);
            logger.debug("Found {} completed milestones for student ID: {}", count, studentId);
            return count;
        } catch (Exception e) {
            logger.error("Error counting completed milestones for student ID: {}", studentId, e);
            return 0;
        }
    }
    
    public long getInProgressMilestonesCount(Long studentId) {
        if (studentId == null) {
            logger.error("Attempted to get in-progress milestones count with null studentId");
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        try {
            long count = milestoneRepository.countByStudentIdAndStatus(studentId, MilestoneStatus.IN_PROGRESS);
            logger.debug("Found {} in-progress milestones for student ID: {}", count, studentId);
            return count;
        } catch (Exception e) {
            logger.error("Error counting in-progress milestones for student ID: {}", studentId, e);
            return 0;
        }
    }
}

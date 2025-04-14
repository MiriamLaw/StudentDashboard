package com.coderscampus.service;

import com.coderscampus.domain.Milestone;
import com.coderscampus.domain.Milestone.MilestoneStatus;
import com.coderscampus.domain.Student;
import com.coderscampus.repository.MilestoneRepository;
import com.coderscampus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MilestoneService {

    @Autowired
    private MilestoneRepository milestoneRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    public List<Milestone> getMilestonesByStudentId(Long studentId) {
        return milestoneRepository.findByStudentId(studentId);
    }
    
    public List<Milestone> getCompletedMilestonesByStudentId(Long studentId) {
        return milestoneRepository.findByStudentIdAndStatus(studentId, MilestoneStatus.COMPLETED);
    }
    
    public List<Milestone> getInProgressMilestonesByStudentId(Long studentId) {
        return milestoneRepository.findByStudentIdAndStatus(studentId, MilestoneStatus.IN_PROGRESS);
    }
    
    public Milestone getMilestoneById(Long id) {
        return milestoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found with id: " + id));
    }
    
    @Transactional
    public Milestone createMilestone(Long studentId, Milestone milestone) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        
        milestone.setStudent(student);
        
        // Set default values
        if (milestone.getStatus() == null) {
            milestone.setStatus(MilestoneStatus.IN_PROGRESS);
        }
        
        return milestoneRepository.save(milestone);
    }
    
    @Transactional
    public Milestone updateMilestone(Long id, Milestone milestoneDetails) {
        Milestone milestone = getMilestoneById(id);
        
        // Update milestone fields
        milestone.setMilestoneName(milestoneDetails.getMilestoneName());
        milestone.setStatus(milestoneDetails.getStatus());
        milestone.setComment(milestoneDetails.getComment());
        
        // If status is changed to completed, set completion date
        if (milestone.getStatus() == MilestoneStatus.COMPLETED && milestone.getCompletionDate() == null) {
            milestone.setCompletionDate(LocalDate.now());
        }
        
        return milestoneRepository.save(milestone);
    }
    
    @Transactional
    public void deleteMilestone(Long id) {
        milestoneRepository.deleteById(id);
    }
    
    @Transactional
    public Milestone completeMilestone(Long id) {
        Milestone milestone = getMilestoneById(id);
        milestone.setStatus(MilestoneStatus.COMPLETED);
        milestone.setCompletionDate(LocalDate.now());
        return milestoneRepository.save(milestone);
    }
    
    public long getCompletedMilestonesCount(Long studentId) {
        return milestoneRepository.countByStudentIdAndStatus(studentId, MilestoneStatus.COMPLETED);
    }
    
    public long getInProgressMilestonesCount(Long studentId) {
        return milestoneRepository.countByStudentIdAndStatus(studentId, MilestoneStatus.IN_PROGRESS);
    }
}

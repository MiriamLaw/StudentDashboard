package com.coderscampus.web;

import com.coderscampus.domain.Student;
import com.coderscampus.domain.VideoProgress;
import com.coderscampus.service.StudentService;
import com.coderscampus.service.VideoProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/video-progress")
public class VideoProgressController {

    @Autowired
    private VideoProgressService videoProgressService;
    
    @Autowired
    private StudentService studentService;
    
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.attributes['email'] == @studentService.getStudentById(#studentId).email")
    public ResponseEntity<List<VideoProgress>> getVideoProgressForStudent(@PathVariable Long studentId) {
        List<VideoProgress> progressList = videoProgressService.getVideoProgressForStudent(studentId);
        return ResponseEntity.ok(progressList);
    }
    
    @GetMapping("/current")
    public ResponseEntity<List<VideoProgress>> getCurrentStudentVideoProgress(Principal principal, 
                                                                            @AuthenticationPrincipal OAuth2User oauth2User) {
        String email = null;
        
        // Handle different authentication types
        if (principal != null) {
            email = principal.getName();
        } else if (oauth2User != null) {
            email = oauth2User.getAttribute("email");
        }
        
        if (email == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Student student = studentService.getStudentByEmail(email);
        List<VideoProgress> progressList = videoProgressService.getVideoProgressForStudent(student.getId());
        return ResponseEntity.ok(progressList);
    }
    
    @GetMapping("/summary/current")
    public ResponseEntity<Map<String, Object>> getCurrentStudentVideoProgressSummary(Principal principal, 
                                                                                   @AuthenticationPrincipal OAuth2User oauth2User) {
        String email = null;
        
        // Handle different authentication types
        if (principal != null) {
            email = principal.getName();
        } else if (oauth2User != null) {
            email = oauth2User.getAttribute("email");
        }
        
        if (email == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Student student = studentService.getStudentByEmail(email);
        List<VideoProgress> progressList = videoProgressService.getVideoProgressForStudent(student.getId());
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalHours", student.getVimeoHoursWatched());
        summary.put("weeklyData", progressList.stream()
                .collect(Collectors.toMap(vp -> "Week " + vp.getWeekNumber(), VideoProgress::getHoursWatched)));
        
        return ResponseEntity.ok(summary);
    }
    
    @PostMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VideoProgress> createVideoProgress(@PathVariable Long studentId, 
                                                          @RequestBody VideoProgress videoProgress) {
        VideoProgress created = videoProgressService.createVideoProgress(studentId, videoProgress);
        return ResponseEntity.ok(created);
    }
} 
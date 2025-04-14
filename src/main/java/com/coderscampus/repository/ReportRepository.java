package com.coderscampus.repository;

import com.coderscampus.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStudentId(Long studentId);
    List<Report> findByStudentIdOrderByReportDateDesc(Long studentId);
    Optional<Report> findByStudentIdAndReportDate(Long studentId, LocalDate reportDate);
}

package com.coderscampus.service;

import com.coderscampus.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    
}

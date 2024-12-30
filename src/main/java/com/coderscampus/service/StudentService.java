package com.coderscampus.service;

import com.coderscampus.domain.Milestone;
import com.coderscampus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;


}

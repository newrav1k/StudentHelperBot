package com.example.service;

import com.example.entity.Student;
import com.example.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    @Cacheable(value = "students", key = "#update.message.from.id")
    public Student save(Update update) {
        return studentRepository.save(update);
    }

    @Transactional
    @Cacheable(value = "students", key = "#id")
    public Student findById(long id) {
        return studentRepository.findById(id).orElse(null);
    }
}
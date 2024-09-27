package com.example.dao;

import com.example.entity.Student;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface StudentDao {
    void insert(Update update);

    void update(Update update);

    void delete(Update update);

    Student findById(Long id);

    List<Student> findAll();
}
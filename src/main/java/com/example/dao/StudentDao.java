package com.example.dao;

import com.example.entity.Student;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface StudentDao {
    void insert(Update update);

    Student findById(Update update);
}
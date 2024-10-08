package com.example.dao;

import com.example.entity.Student;
import com.example.exception.StudentHelperBotException;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface StudentDao {
    void insert(Update update) throws StudentHelperBotException;

    Student findById(Update update) throws StudentHelperBotException;
}
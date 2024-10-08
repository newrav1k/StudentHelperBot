package com.example.dao;

import com.example.entity.Directory;
import com.example.entity.Student;
import com.example.exception.StudentHelperBotException;

import java.util.List;

public interface DirectoryDao {

    void insert(Student student, String title) throws StudentHelperBotException;

    void update(Student student, String title) throws StudentHelperBotException;

    void deleteByTitle(Student student, String title) throws StudentHelperBotException;

    void deleteBySerial(Student student, int id) throws StudentHelperBotException;

    Directory findByTitle(Student student, String title) throws StudentHelperBotException;

    Directory findBySerial(Student student, int serial) throws StudentHelperBotException;

    List<Directory> findAll(Student student) throws StudentHelperBotException;
}
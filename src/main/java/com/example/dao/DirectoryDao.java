package com.example.dao;

import com.example.entity.Directory;
import com.example.entity.Student;

import java.util.List;

public interface DirectoryDao {

    void insert(Student student, String title);

    void update(Student student, String title);

    void deleteByTitle(Student student, String title);

    void deleteBySerial(Student student, int id);

    Directory findByTitle(Student student, String title);

    Directory findBySerial(Student student, int serial);

    List<Directory> findAll(Student student);
}
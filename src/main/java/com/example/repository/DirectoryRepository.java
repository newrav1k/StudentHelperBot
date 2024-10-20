package com.example.repository;

import com.example.entity.Directory;
import com.example.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectoryRepository extends JpaRepository<Directory, Long> {

    void deleteByStudentAndTitle(Student student, String title);

    List<Directory> findAllByStudent(Student student);

    Directory findByStudentAndTitle(Student student, String title);

}
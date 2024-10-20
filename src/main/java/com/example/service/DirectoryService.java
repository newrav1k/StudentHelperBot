package com.example.service;

import com.example.entity.Directory;
import com.example.entity.Student;
import com.example.repository.DirectoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DirectoryService {

    private final DirectoryRepository directoryRepository;

    @Autowired
    public DirectoryService(DirectoryRepository directoryRepository) {
        this.directoryRepository = directoryRepository;
    }

    @Transactional
    public void save(Student student, String title) {
        directoryRepository.save(Directory.builder()
                .student(student)
                .title(title)
                .build());
    }

    @Transactional
    public Directory findByTitle(Student student, String title) {
        return directoryRepository.findAllByStudent(student).stream().filter(d -> d.getTitle().equals(title)).findFirst().orElse(null);
    }

    @Transactional
    public void deleteByTitle(Student student, String title) {
        directoryRepository.deleteByStudentAndTitle(student, title);
    }

    @Transactional
    public List<Directory> findAll(Student student) {
        return directoryRepository.findAllByStudent(student);
    }

    @Transactional
    public void rename(Student student, Directory directory, String newTitle) {
        directory.setTitle(newTitle);
        directory.setStudent(student);
        directoryRepository.save(directory);
    }
}
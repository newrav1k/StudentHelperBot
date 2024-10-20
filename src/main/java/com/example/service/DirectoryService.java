package com.example.service;

import com.example.entity.Directory;
import com.example.entity.Student;
import com.example.repository.DirectoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
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
    @Cacheable(value = "directories", key = "#student.id")
    public Directory save(Student student, String title) {
        Directory directory = null;
        try {
            directory = directoryRepository.save(Directory.builder()
                    .student(student)
                    .title(title)
                    .build());
        } catch (DataIntegrityViolationException ignored) {
            // directory exists
        }
        return directory;
    }

    @Transactional
    @Cacheable(value = "directories", key = "#title")
    public Directory findByTitle(Student student, String title) {
        return directoryRepository.findByStudentAndTitle(student, title);
    }

    @Transactional
    @CachePut(value = "directories", key = "#title")
    public void deleteByTitle(Student student, String title) {
        directoryRepository.deleteByStudentAndTitle(student, title);
    }

    @Transactional
    @CachePut(value = "directories", key = "#newTitle")
    public void rename(Student student, Directory directory, String newTitle) {
        directory.setTitle(newTitle);
        directory.setStudent(student);
        directoryRepository.save(directory);
    }

    @Transactional
    public List<Directory> findAll(Student student) {
        return directoryRepository.findAllByStudent(student);
    }
}
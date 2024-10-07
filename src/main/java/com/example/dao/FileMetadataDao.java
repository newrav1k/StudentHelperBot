package com.example.dao;

import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import com.example.entity.Student;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.util.List;

public interface FileMetadataDao {
    void insert(Update update, Directory directory, File file, Document document);

    void deleteBySerial(Student student, Directory directory, int serial);

    void moveToDirectory(Student student, Directory directory, FileMetadata fileMetadata);

    FileMetadata findBySerial(Student student, Directory directory, int number);

    List<FileMetadata> findAll(Student student, Directory directory);
}
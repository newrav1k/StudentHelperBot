package com.example.dao;

import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import com.example.entity.Student;
import com.example.exception.StudentHelperBotException;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.util.List;

public interface FileMetadataDao {
    void insert(Update update, Directory directory, File file, Document document) throws StudentHelperBotException;

    void deleteBySerial(Student student, Directory directory, int serial) throws StudentHelperBotException;

    void deleteByTitle(Student student, Directory directory, String title) throws StudentHelperBotException;

    void changeFileName(Student student, FileMetadata fileMetadata, String newFileName) throws StudentHelperBotException;

    void moveToDirectory(Student student, Directory directory, FileMetadata fileMetadata) throws StudentHelperBotException;

    FileMetadata findBySerial(Student student, Directory directory, int number) throws StudentHelperBotException;

    List<FileMetadata> findAll(Student student, Directory directory) throws StudentHelperBotException;
}
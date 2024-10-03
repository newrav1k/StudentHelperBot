package com.example.dao;

import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.util.List;

public interface FileMetadataDao {
    void insert(Update update, Directory directory, File file);

    FileMetadata findById(Update update, Directory directory, String number);

    List<File> findAll(Update update, Directory directory);
}
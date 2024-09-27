package com.example.dao;

import com.example.entity.Directory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface DirectoryDao {
    void insert(Update update, String title);

    void update(Update update, String title, String newTitle);

    void delete(Update update, String title);

    Directory findByTitle(Update update, String title);

    List<Directory> findAll(Update update);
}
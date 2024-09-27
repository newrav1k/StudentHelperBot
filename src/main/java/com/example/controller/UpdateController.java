package com.example.controller;

import com.example.dao.DirectoryDao;
import com.example.dao.FileMetadataDao;
import com.example.dao.StudentDao;
import com.example.dao.impl.DirectoryDaoImpl;
import com.example.dao.impl.FileMetadataDaoImpl;
import com.example.dao.impl.StudentDaoImpl;
import com.example.entity.Directory;
import com.example.enums.States;
import com.example.utils.MessageUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface UpdateController {
    StudentDao studentDao = new StudentDaoImpl();
    DirectoryDao directoryDao = new DirectoryDaoImpl();
    FileMetadataDao fileMetadataDao = new FileMetadataDaoImpl();

    Map<Long, States> userStates = new HashMap<>();

    Map<String, List<String>> directoriesAndFiles = new HashMap<>();

    Map<Long, File> previousFiles = new HashMap<>();

    Map<Long, Directory> directories = new HashMap<>();

    MessageUtils messageUtils = new MessageUtils();

    void init(StudentHelperBot studentHelperBot);

    void setView(SendMessage sendMessage);

    default void setUserStates(Update update, States states) {
        Long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }
        userStates.put(chatId, states);
    }

    void processUpdate(Update update);
}
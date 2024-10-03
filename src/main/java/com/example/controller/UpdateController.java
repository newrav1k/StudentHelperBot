package com.example.controller;

import com.example.dao.DirectoryDao;
import com.example.dao.FileMetadataDao;
import com.example.dao.StudentDao;
import com.example.dao.impl.DirectoryDaoImpl;
import com.example.dao.impl.FileMetadataDaoImpl;
import com.example.dao.impl.StudentDaoImpl;
import com.example.enums.States;
import com.example.utils.InformationStorage;
import com.example.utils.MessageUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateController {

    InformationStorage informationStorage = new InformationStorage();

    MessageUtils messageUtils = new MessageUtils();

    StudentDao studentDao = new StudentDaoImpl();

    DirectoryDao directoryDao = new DirectoryDaoImpl();

    FileMetadataDao fileMetadataDao = new FileMetadataDaoImpl();

    void init(StudentHelperBot studentHelperBot);

    void setView(SendMessage sendMessage);

    default void setUserStates(Update update, States states) {
        long id;
        if (update.hasCallbackQuery()) {
            id = update.getCallbackQuery().getFrom().getId();
        } else {
            id = update.getMessage().getFrom().getId();
        }
        informationStorage.putState(id, states);
    }

    void processUpdate(Update update);
}
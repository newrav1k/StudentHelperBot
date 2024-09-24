package com.example.controller;

import com.example.enums.States;
import com.example.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface UpdateController {
    Logger log = LoggerFactory.getLogger(UpdateController.class);

    Map<Long, States> userStates = new HashMap<>();

//    List<String> directories = new ArrayList<>();
    Map<String, List<String>> directoriesAndFiles = new HashMap<>();

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
        log.info("Для пользователя {} установлено состояние {}", chatId, states);
    }

    void processUpdate(Update update);
}
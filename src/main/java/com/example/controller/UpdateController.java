package com.example.controller;

import com.example.enums.States;
import com.example.utils.MessageUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface UpdateController {
    Map<Long, States> userStates = new HashMap<>();

    List<String> directories = new ArrayList<>();

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
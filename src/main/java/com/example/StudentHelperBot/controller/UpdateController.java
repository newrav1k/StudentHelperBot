package com.example.StudentHelperBot.controller;

import com.example.StudentHelperBot.utils.MessageUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateController {
    MessageUtils messageUtils = new MessageUtils();

    void init(StudentHelperBot studentHelperBot);

    void setView(SendMessage sendMessage);

    void processUpdate(Update update);
}
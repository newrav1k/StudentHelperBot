package com.example.StudentHelperBot.controller.type;

import com.example.StudentHelperBot.controller.StudentHelperBot;
import com.example.StudentHelperBot.controller.UpdateController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Repository
@Qualifier("PhotoController")
public class PhotoController implements UpdateController {
    private static final Logger log = LoggerFactory.getLogger(PhotoController.class);

    private StudentHelperBot studentHelperBot;

    @Override
    public void processUpdate(Update update) {
        setView(messageUtils.generateSendMessageWithText(update, "Фотография получена..."));
    }

    @Override
    public void init(StudentHelperBot studentHelperBot) {
        this.studentHelperBot = studentHelperBot;
    }

    @Override
    public void setView(SendMessage sendMessage) {
        studentHelperBot.sendAnswerMessage(sendMessage);
    }
}
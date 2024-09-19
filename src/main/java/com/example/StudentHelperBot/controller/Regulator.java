package com.example.StudentHelperBot.controller;

import com.example.StudentHelperBot.controller.type.CallbackDataController;
import com.example.StudentHelperBot.controller.type.DocumentController;
import com.example.StudentHelperBot.controller.type.PhotoController;
import com.example.StudentHelperBot.controller.type.TextController;
import com.example.StudentHelperBot.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class Regulator {
    private static final Logger log = LoggerFactory.getLogger(Regulator.class);

    private final UpdateController updateController;
    private final MessageUtils messageUtils;

    public Regulator(UpdateController updateController, MessageUtils messageUtils) {
        this.updateController = updateController;
        this.messageUtils = messageUtils;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Объект не может быть null");
            return;
        }
        if (update.hasCallbackQuery()) {
            new CallbackDataController().processUpdate(update);
        } else if (update.getMessage() != null) {
            distributeMessageByType(update);
        } else {
            log.error("Неизвестный тип сообщения {}", update);
        }
    }

    private void distributeMessageByType(Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            processTextMessage(update);
        } else if (message.hasDocument()) {
            processDocMessage(update);
        } else if (message.hasPhoto()) {
            processPhotoMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setUnsupportedMessageTypeView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
                "Неподдерживаемый тип сообщения!", false);
        updateController.setView(sendMessage);
    }

    private void processPhotoMessage(Update update) {
        new PhotoController().processUpdate(update);
    }

    private void processDocMessage(Update update) {
        new DocumentController().processUpdate(update);
    }

    private void processTextMessage(Update update) {
        new TextController().processUpdate(update);
    }
}
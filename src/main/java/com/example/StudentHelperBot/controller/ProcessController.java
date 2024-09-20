package com.example.StudentHelperBot.controller;

import com.example.StudentHelperBot.controller.type.CallbackDataController;
import com.example.StudentHelperBot.controller.type.DocumentController;
import com.example.StudentHelperBot.controller.type.PhotoController;
import com.example.StudentHelperBot.controller.type.TextController;
import com.example.StudentHelperBot.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Repository
public class ProcessController {
    private static final Logger log = LoggerFactory.getLogger(ProcessController.class);

    private final MessageUtils messageUtils;

    @Autowired
    private ApplicationContext applicationContext;

    public ProcessController(MessageUtils messageUtils) {
        this.messageUtils = messageUtils;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Объект не может быть null");
            return;
        }
        if (update.hasCallbackQuery()) {
            processCallbackDataMessage(update);
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
                "Неподдерживаемый тип сообщения!");
        applicationContext.getBean(TextController.class).setView(sendMessage);
    }

    private void processPhotoMessage(Update update) {
        applicationContext.getBean(PhotoController.class).processUpdate(update);
    }

    private void processDocMessage(Update update) {
        applicationContext.getBean(DocumentController.class).processUpdate(update);
    }

    private void processTextMessage(Update update) {
        applicationContext.getBean(TextController.class).processUpdate(update);
    }

    private void processCallbackDataMessage(Update update) {
        applicationContext.getBean(CallbackDataController.class).processUpdate(update);
    }
}
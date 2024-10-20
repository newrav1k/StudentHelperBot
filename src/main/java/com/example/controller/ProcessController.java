package com.example.controller;

import com.example.controller.type.CallbackDataController;
import com.example.controller.type.DocumentController;
import com.example.controller.type.PhotoController;
import com.example.controller.type.TextController;
import com.example.utils.InformationStorage;
import com.example.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class ProcessController {

    private final MessageUtils messageUtils;

    private final ApplicationContext applicationContext;

    private final InformationStorage informationStorage;

    @Autowired
    public ProcessController(MessageUtils messageUtils, ApplicationContext applicationContext,
                             InformationStorage informationStorage) {
        this.messageUtils = messageUtils;
        this.applicationContext = applicationContext;
        this.informationStorage = informationStorage;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Объект не может быть null");
            return;
        }
        long id = -1;
        if (update.hasCallbackQuery()) {
            id = update.getCallbackQuery().getFrom().getId();
            processCallbackDataMessage(update);
        } else if (update.getMessage() != null) {
            distributeMessageByType(update);
            id = update.getMessage().getFrom().getId();
        } else {
            log.error("Неизвестный тип сообщения {}", update);
        }
        informationStorage.putUpdate(id, update);
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
        applicationContext.getBean("textController", TextController.class).setView(sendMessage);
    }

    private void processPhotoMessage(Update update) {
        applicationContext.getBean("photoController", PhotoController.class).processUpdate(update);
    }

    private void processDocMessage(Update update) {
        applicationContext.getBean("documentController", DocumentController.class).processUpdate(update);
    }

    private void processTextMessage(Update update) {
        applicationContext.getBean("textController", TextController.class).processUpdate(update);
    }

    private void processCallbackDataMessage(Update update) {
        applicationContext.getBean("callbackDataController", CallbackDataController.class).processUpdate(update);
    }
}
package com.example.StudentHelperBot.controller;

import com.example.StudentHelperBot.service.AnswerConsumer;
import com.example.StudentHelperBot.service.UpdateProducer;
import com.example.StudentHelperBot.utils.MessageUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.example.StudentHelperBot.controller.StudentHelperBot.*;
import static com.example.StudentHelperBot.service.impl.UpdateProducerImpl.*;

@Component
public class UpdateController {
    private static final Logger log = LogManager.getLogger(UpdateController.class);

    private StudentHelperBot studentHelperBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;
    private final AnswerConsumer answerConsumer;

    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer, AnswerConsumer answerConsumer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
        this.answerConsumer = answerConsumer;
    }

    public void registerBot(StudentHelperBot studentHelperBot) {
        this.studentHelperBot = studentHelperBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Объект не может быть null");
            return;
        }
        if (update.getMessage() != null) {
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
        setView(sendMessage);
    }

    private void setStartView(Update update) {
        String message = String.format("Привет, %s! Я телеграмм-бот, созданный группой замечательных людей\n" +
                "Для ознакомления с уже доступным функционалом введите /help", update.getMessage().getChat().getFirstName());
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update, message);
        setView(sendMessage);
    }

    private void setFileIsReceivedView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
                "Файл получен! Обрабатывается...");
        setView(sendMessage);
    }

    private void setHelpView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
                """
                        ⚙️ Команды
                        /start - описание и перезапуск бота\s
                        /upload_file – загрузить файл на сервер""");
        setView(sendMessage);
    }

    private void setView(SendMessage sendMessage) {
        studentHelperBot.sendAnswerMessage(sendMessage);
    }

    private void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
    }

    private void processDocMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }

    private void processTextMessage(Update update) {
        String message = update.getMessage().getText();
        switch (message) {
            case START -> setStartView(update);
            case UPLOAD_FILE -> setFileIsReceivedView(update);
            case HELP -> setHelpView(update);
            default -> updateProducer.produce(message, update);
        }
    }
}
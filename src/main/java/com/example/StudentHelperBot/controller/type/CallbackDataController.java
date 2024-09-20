package com.example.StudentHelperBot.controller.type;

import com.example.StudentHelperBot.controller.StudentHelperBot;
import com.example.StudentHelperBot.controller.UpdateController;
import com.example.StudentHelperBot.enums.CallbackData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Repository
@Qualifier("CallbackDataController")
public class CallbackDataController implements UpdateController {
    private static final Logger log = LoggerFactory.getLogger(CallbackDataController.class);

    private StudentHelperBot studentHelperBot;

    @Override
    public void processUpdate(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        switch (CallbackData.fromString(data)) {
            case CALLBACK_DATA_SAVE -> saveProcess(update);
            case CALLBACK_DATA_CONVERT -> convertProcess(update);
            case CALLBACK_DATA_DELETE -> deleteProcess(update);
            case CALLBACK_DATA_CANCEL -> cancelProcess(update);
        }
    }

    @Override
    public void init(StudentHelperBot studentHelperBot) {
        this.studentHelperBot = studentHelperBot;
    }

    @Override
    public void setView(SendMessage sendMessage) {
        studentHelperBot.sendAnswerMessage(sendMessage);
    }

    private void saveProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Нажата кнопка сохранения"));
    }

    private void convertProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Нажата кнопка конвертации"));
    }

    private void deleteProcess(Update update) {
        log.info(update.getCallbackQuery().getData());
    }

    private void cancelProcess(Update update) {
        log.info(update.getCallbackQuery().getData());
    }
}
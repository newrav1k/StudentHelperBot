package com.example.StudentHelperBot.controller.type;

import com.example.StudentHelperBot.controller.StudentHelperBot;
import com.example.StudentHelperBot.controller.UpdateController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CallbackDataController implements UpdateController {
    private static final Logger log = LoggerFactory.getLogger(CallbackDataController.class);

    private static final String CALLBACK_DATA_SAVE = "callback_data_save";
    private static final String CALLBACK_DATA_CONVERT = "callback_data_convert";
    private static final String CALLBACK_DATA_DELETE = "callback_data_delete";
    private static final String CALLBACK_DATA_CANCEL = "callback_data_cancel";

    private StudentHelperBot studentHelperBot;

    @Override
    public void processUpdate(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        switch (data) {
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
        log.info(update.getCallbackQuery().getData());
    }

    private void convertProcess(Update update) {
        log.info(update.getCallbackQuery().getData());
    }

    private void deleteProcess(Update update) {
        log.info(update.getCallbackQuery().getData());
    }

    private void cancelProcess(Update update) {
        log.info(update.getCallbackQuery().getData());
    }
}
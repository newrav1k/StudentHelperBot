package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.enums.CallbackData;
import com.example.enums.States;
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
@Qualifier("callbackDataController")
public class CallbackDataController implements UpdateController {
    private static final Logger log = LoggerFactory.getLogger(CallbackDataController.class);

    private StudentHelperBot studentHelperBot;

    @Override
    public void processUpdate(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
//        Придумать удаление последнего сообщения с кнопками, чтобы избежать их повторного нажатия
        switch (CallbackData.fromString(data)) {
            case CALLBACK_DATA_SAVE -> saveProcess(update);
            case CALLBACK_DATA_CONVERT -> convertProcess(update);
            case CALLBACK_DATA_DELETE -> deleteProcess(update);
            case CALLBACK_DATA_CANCEL -> cancelProcess(update);
            case CALLBACK_DATA_ADD -> addProcess(update);
            case CALLBACK_DATA_CHOOSE -> chooseProcess(update);
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
        log.info(update.getCallbackQuery().getData());
    }

    private void convertProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Нажата кнопка конвертации"));
        log.info(update.getCallbackQuery().getData());
    }

    private void cancelProcess(Update update) {
        setUserStates(update, States.ACTIVE);
        log.info("Для пользователя {} установлено состояние {}",
                update.getCallbackQuery().getFrom().getUserName(), States.ACTIVE);
    }

    private void addProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Нажата кнопка добавить \n" +
                "Введите название новой директории:"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_ADD);
        log.info("Для пользователя {} установлено состояние {}",
                update.getCallbackQuery().getFrom().getUserName(), States.WAITING_DIRECTORY_NAME_ADD);
    }

    private void deleteProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Нажата кнопка удалить \n" +
                "Введите название директории, которую хотите удалить:"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_DELETE);
        log.info("Для пользователя {} установлено состояние {}",
                update.getCallbackQuery().getFrom().getUserName(), States.WAITING_DIRECTORY_NAME_DELETE);
    }

    private void chooseProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Нажата кнопка выбрать"));
        log.info(update.getCallbackQuery().getData());
    }
}
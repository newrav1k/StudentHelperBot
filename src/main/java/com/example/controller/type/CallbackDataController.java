package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.enums.CallbackData;
import com.example.enums.States;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@Repository
@Qualifier("callbackDataController")
public class CallbackDataController implements UpdateController {

    @Setter
    private static String inlineKeyboardText;

    private StudentHelperBot studentHelperBot;

    @Override
    public void processUpdate(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        deleteInlineKeyboard(update);
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
    }

    private void convertProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Нажата кнопка конвертации"));
    }

    private void cancelProcess(Update update) {
        setUserStates(update, States.ACTIVE);
    }

    private void addProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Введите название новой директории:"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_ADD);
    }

    private void deleteProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Введите название директории, которую хотите удалить:"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_DELETE);
    }

    private void chooseProcess(Update update) {
        setUserStates(update, States.WAITING_FILE_NAME_CHOOSE);
        log.info(update.getCallbackQuery().getData());
    }

    private void deleteInlineKeyboard(Update update) {
        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String text = inlineKeyboardText;
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setReplyMarkup(null);
        studentHelperBot.sendEditMessage(editMessage);
    }
}
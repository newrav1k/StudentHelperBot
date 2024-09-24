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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Repository
@Qualifier("callbackDataController")
public class CallbackDataController implements UpdateController {
    private static final Logger log = LoggerFactory.getLogger(CallbackDataController.class);
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
            case CALLBACK_DATA_DELETE_DIRECTORY -> deleteDirectoryProcess(update);
            case CALLBACK_DATA_CANCEL -> cancelProcess(update);
            case CALLBACK_DATA_ADD_DIRECTORY -> addDirectoryProcess(update);
            case CALLBACK_DATA_CHOOSE -> chooseProcess(update);
            case CALLBACK_DATA_ADD_FILE -> addFileProcess(update);
            case CALLBACK_DATA_DOWNLOAD_FILE -> downloadFileProcess(update);
            case CALLBACK_DATA_DELETE_FILE -> deleteFileProcess(update);
            case CALLBACK_DATA_CHANGE_FILE_DIRECTORY -> changeFileDirectoryProcess(update);
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

    private void addDirectoryProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Введите название новой директории:"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_ADD);
    }

    private void chooseProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Введите навзание директории, в которую хотите перейти:"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_CHOOSE);
    }

    private void deleteDirectoryProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Введите название директории, которую хотите удалить:"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_DELETE);
    }

    private void addFileProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Загрузите файл, который хотите добавить:"));
        setUserStates(update, States.WAITING_FILE_NAME_ADD);
    }

    private void downloadFileProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Введите название файла, который хотите скачать:"));
        setUserStates(update, States.WAITING_FILE_NAME_DOWNLOAD);
    }

    private void deleteFileProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Введите название файла, который хотите удалить:"));
        setUserStates(update, States.WAITING_FILE_NAME_DELETE);
    }

    private void changeFileDirectoryProcess(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Введите название файла, который хотите перенести в другую директорию:"));
        setUserStates(update, States.WAITING_FILE_NAME_FOR_CHANGE);
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

    public static void setInlineKeyboardText(String newInlineKeyboardText) {
        inlineKeyboardText = newInlineKeyboardText;
    }
}
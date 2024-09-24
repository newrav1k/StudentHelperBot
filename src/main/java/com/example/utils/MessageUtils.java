package com.example.utils;

import com.example.controller.type.CallbackDataController;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MessageUtils {
    public SendMessage generateSendMessageWithCallbackData(Update update, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setText(text);
        return sendMessage;
    }

    public SendMessage generateSendMessageWithText(Update update, String text) {
        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(text);
        return sendMessage;
    }

    public SendMessage generateSendMessageForDocument(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = uploadingFile();

        // Устанавливаем кнопки в markup
        markup.setKeyboard(rows);

        String text = "Выберите что хотите сделать:";
        CallbackDataController.setInlineKeyboardText(text);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

    public SendMessage generateSendMessageForDocumentSelection(Update update, String suggestion) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = actionSelection();

        // Устанавливаем кнопки в markup
        markup.setKeyboard(rows);

        CallbackDataController.setInlineKeyboardText(suggestion);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(suggestion);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

    public SendMessage generateSendMessageForDirectories(Update update, Map<String, List<String>> directoriesAndFiles) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = getDirectoryRows();

         // Устанавливаем кнопки в markup
        markup.setKeyboard(rows);

        String text = "Список директорий из базы данных:" + "\n" + buildDirectoriesList(directoriesAndFiles);
        CallbackDataController.setInlineKeyboardText(text);

         // Создаем сообщение
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(markup);

        // Отправляем сообщение

        return sendMessage;
    }

    public SendMessage generateSendMessageForFiles(Update update, Map<String, List<String>> directoriesAndFiles) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = getFilesRows();

         // Устанавливаем кнопки в markup
        markup.setKeyboard(rows);

        String text = "Список файлов директории " + update.getMessage().getText() + ":" + "\n" + buildFilesList(directoriesAndFiles, update);
        CallbackDataController.setInlineKeyboardText(text);

         // Создаем сообщение
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(markup);

        // Отправляем сообщение

        return sendMessage;
    }

    private List<List<InlineKeyboardButton>> uploadingFile() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Создаем первую строку с кнопками
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Сохранить"); // Текст на кнопке
        button1.setCallbackData("callback_data_save"); // Данные для обратного вызова
        row1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Конвертировать");
        button2.setCallbackData("callback_data_convert");
        row1.add(button2);

        // Создаем вторую строку с кнопками
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Отмена");
        button3.setCallbackData("callback_data_cancel");
        row2.add(button3);

        rows.add(row1);
        rows.add(row2);
        return rows;
    }

    private List<List<InlineKeyboardButton>> getDirectoryRows() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Добавить"); // Текст на кнопке
        button1.setCallbackData("callback_data_add_directory"); // Данные для обратного вызова
        row1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Выбрать");
        button2.setCallbackData("callback_data_choose");
        row1.add(button2);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Удалить");
        button3.setCallbackData("callback_data_delete_directory");
        row2.add(button3);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("Отмена");
        button4.setCallbackData("callback_data_cancel");
        row3.add(button4);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        return rows;
    }

    private List<List<InlineKeyboardButton>> getFilesRows() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Добавить"); // Текст на кнопке
        button1.setCallbackData("callback_data_add_file"); // Данные для обратного вызова
        row1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Скачать");
        button2.setCallbackData("callback_data_download_file");
        row1.add(button2);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Изменить");
        button3.setCallbackData("callback_data_change_file_directory");
        row2.add(button3);

        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("Удалить");
        button4.setCallbackData("callback_data_delete_file");
        row2.add(button4);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton button5 = new InlineKeyboardButton();
        button5.setText("Отмена");
        button5.setCallbackData("callback_data_cancel");
        row3.add(button5);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        return rows;
    }

    private List<List<InlineKeyboardButton>> actionSelection() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Отмена");
        button1.setCallbackData("callback_data_cancel");
        row1.add(button1);

        rows.add(row1);
        return rows;
    }

    public String buildDirectoriesList(Map<String, List<String>> directoriesAndFiles) {
        StringBuilder directoriesForSendMessage = new StringBuilder();
        List<String> directoriesList = new ArrayList<>(directoriesAndFiles.keySet());
        for (String directory : directoriesList) {
            directoriesForSendMessage.append(directoriesList.indexOf(directory) + 1).append(".").append(directory).append("\n");
        }
        return directoriesForSendMessage.toString();
    }

    public String buildFilesList(Map<String, List<String>> directoriesAndFiles, Update update) {
        StringBuilder filesForSendMessage = new StringBuilder();
        List<String> filesList = new ArrayList<>(directoriesAndFiles.get(update.getMessage().getText()));
        for (String file : filesList) {
            filesForSendMessage.append(filesList.indexOf(file) + 1).append(".").append(file).append("\n");
        }
        return filesForSendMessage.toString();
    }
}
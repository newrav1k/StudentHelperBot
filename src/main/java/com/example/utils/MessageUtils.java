package com.example.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

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

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Выберите что хотите сделать:");
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

    public SendMessage generateSendMessageForDocumentSelection(Update update, String suggestion) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = actionSelection();

        // Устанавливаем кнопки в markup
        markup.setKeyboard(rows);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(suggestion);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

    public SendMessage generateSendMessageForDirectories(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = getDirectoryRows();

         // Устанавливаем кнопки в markup
        markup.setKeyboard(rows);

         // Создаем сообщение
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Список директорий из базы данных:");
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
        button1.setCallbackData("callback_data_add"); // Данные для обратного вызова
        row1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Выбрать");
        button2.setCallbackData("callback_data_choose");
        row1.add(button2);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Удалить");
        button3.setCallbackData("callback_data_delete");
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
}
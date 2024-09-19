package com.example.StudentHelperBot.utils;

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
    public SendMessage generateSendMessageWithText(Update update, String text, boolean isCallbackData) {
        return isCallbackData ? generateForCallbackData(update, text) : generateForUsualMessage(update, text);
    }

    private SendMessage generateForCallbackData(Update update, String text) {
        return new SendMessage(update.getCallbackQuery().getMessage().getChatId().toString(), text);
    }

    private SendMessage generateForUsualMessage(Update update, String text) {
        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
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

    public SendMessage generateSendMessageForDocumentTypeMessage(Update update) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = getFileRows();

        // Устанавливаем кнопки в markup
        markup.setKeyboard(rows);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Выберите что хотите сделать:");
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

    private List<List<InlineKeyboardButton>> getFileRows() {
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

        rows.add(row1);
        return rows;
    }
}
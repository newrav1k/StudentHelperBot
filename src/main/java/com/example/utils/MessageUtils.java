package com.example.utils;

import com.example.controller.type.CallbackDataController;
import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MessageUtils {

    private static final Map<String, String> EMOJI_MAP = new HashMap<>();

    static {
        EMOJI_MAP.put("png", "🖼");
        EMOJI_MAP.put("jpg", "🖼");
        EMOJI_MAP.put("txt", "📜");
        EMOJI_MAP.put("docx", "📘");
        EMOJI_MAP.put("pptx", "📕");
        EMOJI_MAP.put("xlsx", "📗");
        EMOJI_MAP.put("pdf", "📓");
    }

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

    public SendMessage generateSendMessageLookingForward(Update update, String suggestion) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = actionSelection();

        // Устанавливаем кнопки в markup
        markup.setKeyboard(rows);

        CallbackDataController.setInlineKeyboardText(suggestion);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.hasCallbackQuery() ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId());
        sendMessage.setText(suggestion);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

    public SendMessage generateSendMessageForDirectories(Update update, List<Directory> directories) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = getDirectoryRows();

         // Устанавливаем кнопки в markup
        markup.setKeyboard(rows);

        String text = "Список директорий из базы данных:\n" + buildDirectoriesList(directories);
        CallbackDataController.setInlineKeyboardText(text);

         // Создаем сообщение
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(markup);

        // Отправляем сообщение

        return sendMessage;
    }

    public SendMessage generateSendMessageForFiles(Update update, List<FileMetadata> files, Directory directory) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = getFilesRows();

         // Устанавливаем кнопки в markup
        markup.setKeyboard(rows);

        String text = directory.getTitle() + ":\n" + buildFilesList(files);
        CallbackDataController.setInlineKeyboardText(text);

         // Создаем сообщение
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(markup);

        // Отправляем сообщение
        return sendMessage;
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Загрузить файл"));
        row1.add(new KeyboardButton("Конвертировать файл"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Отобразить директории"));

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);

        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        return replyKeyboardMarkup;
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
        button2.setCallbackData("callback_data_choose_directory");
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
        button1.setText("Загрузить файл"); // Текст на кнопке
        button1.setCallbackData("callback_data_add_file"); // Данные для обратного вызова
        row1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Скачать файл");
        button2.setCallbackData("callback_data_download_file");
        row1.add(button2);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Изменить директорию");
        button3.setCallbackData("callback_data_change_file_directory");
        row2.add(button3);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("Поменять название");
        button4.setCallbackData("callback_data_change_file_name");
        row3.add(button4);

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        InlineKeyboardButton button5 = new InlineKeyboardButton();
        button5.setText("Удалить");
        button5.setCallbackData("callback_data_delete_file");
        row4.add(button5);

        InlineKeyboardButton button6 = new InlineKeyboardButton();
        button6.setText("Отмена");
        button6.setCallbackData("callback_data_cancel");
        row4.add(button6);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        rows.add(row4);
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

    private String buildDirectoriesList(List<Directory> directories) {
        StringBuilder directoriesForSendMessage = new StringBuilder();
        for (Directory directory : directories) {
            directoriesForSendMessage.append(directories.indexOf(directory) + 1)
                    .append(") ").append(directory.getTitle()).append(" 🗂").append("\n");
        }
        return directoriesForSendMessage.toString();
    }

    private String buildFilesList(List<FileMetadata> files) {
        StringBuilder filesForSendMessage = new StringBuilder();
        int i = 0;
        for (FileMetadata file : files) {
            String fileName = file.getTitle();
            filesForSendMessage.append(++i)
                    .append(") ")
                    .append(fileName, 0, (fileName.length() < 32 ? fileName.lastIndexOf(".") : fileName.lastIndexOf(".") / 2))
                    .append(fileName.substring(fileName.lastIndexOf("."))).append(" ")
                    .append(EMOJI_MAP.getOrDefault(file.getExtension(), "⚙")).append("\n");
        }
        return filesForSendMessage.toString();
    }
}
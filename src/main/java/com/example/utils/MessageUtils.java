package com.example.utils;

import com.example.controller.type.CallbackDataController;
import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.LinkPreviewOptions;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;

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

    public List<SendMessage> generateSendMessageAboutDevelopers(Update update) {
        List<SendMessage> list = new ArrayList<>();
        Long chatId = update.getMessage().getChatId();

        InlineKeyboardMarkup markup1 = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows1 = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        List<MessageEntity> entities1 = new ArrayList<>();
        final String info1 = """
                Разработчик: Nisanchik
                Почта: emildaniil@gmail.com
                Телеграмм: @Nisan2004""";
        SendMessage sendMessage1 = new SendMessage(chatId.toString(), info1);

        MessageEntity developerEntity1 = new MessageEntity("bold", info1.indexOf("Разработчик"), 11);
        MessageEntity emailEntity1 = new MessageEntity("bold", info1.indexOf("Почта"), 5);
        MessageEntity telegramEntity1 = new MessageEntity("bold", info1.indexOf("Телеграмм"), 9);
        entities1.add(developerEntity1);
        entities1.add(emailEntity1);
        entities1.add(telegramEntity1);
        sendMessage1.setEntities(entities1);

        InlineKeyboardButton b1 = new InlineKeyboardButton("Перейти в профиль");
        b1.setWebApp(new WebAppInfo("https://github.com/Nisanchik"));
        buttons1.add(b1);
        rows1.add(buttons1);
        markup1.setKeyboard(rows1);
        sendMessage1.setReplyMarkup(markup1);

        LinkPreviewOptions linkPreviewOptions1 = new LinkPreviewOptions();
        linkPreviewOptions1.setUrlField("https://github.com/Nisanchik");
        sendMessage1.setLinkPreviewOptions(linkPreviewOptions1);
        list.add(sendMessage1);

        InlineKeyboardMarkup markup2 = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows2 = new ArrayList<>();
        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        List<MessageEntity> entities2 = new ArrayList<>();
        final String info2 = """
                Разработчик: newrav1k
                Почта: kritsky.academi@gmail.com
                Телеграмм: @newrav1k""";
        SendMessage sendMessage2 = new SendMessage(chatId.toString(), info2);

        MessageEntity developerEntity2 = new MessageEntity("bold", info2.indexOf("Разработчик"), 11);
        MessageEntity emailEntity2 = new MessageEntity("bold", info2.indexOf("Почта"), 5);
        MessageEntity telegramEntity2 = new MessageEntity("bold", info2.indexOf("Телеграмм"), 9);
        entities2.add(developerEntity2);
        entities2.add(emailEntity2);
        entities2.add(telegramEntity2);
        sendMessage2.setEntities(entities2);

        InlineKeyboardButton b2 = new InlineKeyboardButton("Перейти в профиль");
        b2.setWebApp(new WebAppInfo("https://github.com/newrav1k"));
        buttons2.add(b2);
        rows2.add(buttons2);
        markup2.setKeyboard(rows2);
        sendMessage2.setReplyMarkup(markup2);

        LinkPreviewOptions linkPreviewOptions2 = new LinkPreviewOptions();
        linkPreviewOptions2.setUrlField("https://github.com/newrav1k");
        sendMessage2.setLinkPreviewOptions(linkPreviewOptions2);
        list.add(sendMessage2);

        return list;
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
        button3.setText("Переименовать");
        button3.setCallbackData("callback_data_change_directory_name");
        row2.add(button3);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("Удалить");
        button4.setCallbackData("callback_data_delete_directory");
        row3.add(button4);

        List<InlineKeyboardButton> row4 = new ArrayList<>();
        InlineKeyboardButton button5 = new InlineKeyboardButton();
        button5.setText("Отмена");
        button5.setCallbackData("callback_data_cancel");
        row4.add(button5);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        rows.add(row4);

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
        button6.setCallbackData("callback_data_cancel_file");
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

    public EditMessageText editSendMessageForDirectories(Update update, List<Directory> directories) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = getDirectoryRows();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        // Устанавливаем кнопки в markup
        markup.setKeyboard(rows);

        String text = "Список директорий из базы данных:\n" + buildDirectoriesList(directories);
        CallbackDataController.setInlineKeyboardText(text);

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setReplyMarkup(markup);

        // Отправляем сообщение

        return editMessage;
    }

    public EditMessageText editDirectoriesMessageWithChooseButtons(Update update, String text, List<Directory> directories, String action) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        CallbackDataController.setInlineKeyboardText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = directoriesActionInlineKeyboard(directories, action);

        markup.setKeyboard(rows);

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setReplyMarkup(markup);

        return editMessage;
    }

    private List<List<InlineKeyboardButton>> directoriesActionInlineKeyboard(List<Directory> directories, String action) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int i = 0; i < directories.size(); i += 2) {
            List<InlineKeyboardButton> row = getInlineKeyboardButtons(directories, action, i);
            rows.add(row);
        }

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Отмена");
        button1.setCallbackData("callback_data_cancel_for_directories_action");
        row1.add(button1);

        rows.add(row1);

        return rows;
    }

    public EditMessageText editSendMessageForFiles(Update update, List<FileMetadata> files, Directory directory) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = getFilesRows();
        EditMessageText editMessage = new EditMessageText();

        String text = directory.getTitle() + ":\n" + buildFilesList(files);
        CallbackDataController.setInlineKeyboardText(text);

        markup.setKeyboard(rows);

        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setReplyMarkup(markup);

        return editMessage;
    }

    public EditMessageText editFilesMessageWithChooseButtons(Update update, String text, List<FileMetadata> files, String action) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        CallbackDataController.setInlineKeyboardText(text);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = filesActionInlineKeyboard(files, action);

        EditMessageText editMessage = new EditMessageText();

        markup.setKeyboard(rows);

        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setReplyMarkup(markup);

        return editMessage;
    }

    private List<List<InlineKeyboardButton>> filesActionInlineKeyboard(List<FileMetadata> files, String action) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int i = 0; i < files.size(); i += 2) {
            List<InlineKeyboardButton> row = getKeyboardButtons(files, action, i);
            rows.add(row);
        }

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Отмена");
        button1.setCallbackData("callback_data_cancel_for_files_action");
        row1.add(button1);

        rows.add(row1);

        return rows;
    }

    public EditMessageText directoryDeletionConfirmation(Update update, String text, Directory directory) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = getConfirmationRows();
        EditMessageText editMessage = new EditMessageText();

        CallbackDataController.setInlineKeyboardText(text);

        markup.setKeyboard(rows);

        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text + " " + directory.getTitle() + "?");
        editMessage.setReplyMarkup(markup);

        return editMessage;
    }

    public EditMessageText fileDeletionConfirmation(Update update, String text, FileMetadata fileMetadata) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = getConfirmationRows();
        EditMessageText editMessage = new EditMessageText();

        CallbackDataController.setInlineKeyboardText(text);

        markup.setKeyboard(rows);

        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text + " " + fileMetadata.getTitle() + "?");
        editMessage.setReplyMarkup(markup);

        return editMessage;
    }

    private List<List<InlineKeyboardButton>> getConfirmationRows() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Да"); // Текст на кнопке
        button1.setCallbackData("callback_data_directory_confirmation_yes"); // Данные для обратного вызова
        row1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Нет");
        button2.setCallbackData("callback_data_directory_confirmation_no");
        row1.add(button2);

        rows.add(row1);

        return rows;
    }

    private static List<InlineKeyboardButton> getInlineKeyboardButtons(List<Directory> directories, String action, int i) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText(directories.get(i).getTitle());
        button1.setCallbackData("callback_data_" + action + "_" + directories.get(i).getTitle());
        row.add(button1);
        if (i + 1 < directories.size()) {
            InlineKeyboardButton button2 = new InlineKeyboardButton();
            button2.setText(directories.get(i + 1).getTitle());
            button2.setCallbackData("callback_data_" + action + "_" + directories.get(i + 1).getTitle());
            row.add(button2);
        }
        return row;
    }

    private static List<InlineKeyboardButton> getKeyboardButtons(List<FileMetadata> files, String action, int i) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText((i + 1) + ". " + files.get(i).getTitle());
        button1.setCallbackData("callback_data_" + action + "_" + (i + 1));
        row.add(button1);
        if (i + 1 < files.size()) {
            InlineKeyboardButton button2 = new InlineKeyboardButton();
            button2.setText((i + 2) + ". " + files.get(i + 1).getTitle());
            button2.setCallbackData("callback_data_" + action + "_" + (i + 2));
            row.add(button2);
        }
        return row;
    }
}
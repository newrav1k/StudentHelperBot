package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.enums.States;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
@Repository
@Qualifier("documentController")
public class DocumentController implements UpdateController {

    private StudentHelperBot studentHelperBot;

    @Override
    public void processUpdate(Update update) {
        Long chatId = update.getMessage().getChatId();
        States states = userStates.getOrDefault(chatId, States.ACTIVE);

        switch (states) {
            case ACTIVE -> producerProcess(update);
            case WAITING_FILE_NAME_ADD -> saveProcess(update);
            default -> log.error("Что-то пошло не так");
        }

        File file = null;
        try {
            file = studentHelperBot.execute(new GetFile(update.getMessage().getDocument().getFileId()));
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
        previousFiles.put(chatId, file);
    }

    @Override
    public void init(StudentHelperBot studentHelperBot) {
        this.studentHelperBot = studentHelperBot;
    }

    @Override
    public void setView(SendMessage sendMessage) {
        studentHelperBot.sendAnswerMessage(sendMessage);
    }

    private void producerProcess(Update update) {
        setView(messageUtils.generateSendMessageForDocument(update));
    }

    private void saveProcess(Update update) {
        Document document = update.getMessage().getDocument();
        try {
            File execute = studentHelperBot.execute(new GetFile(document.getFileId()));
            java.io.File downloadFile = studentHelperBot.downloadFile(execute);
            fileMetadataDao.insert(update, downloadFile);
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
        setUserStates(update, States.ACTIVE);
    }
}
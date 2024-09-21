package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.enums.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Repository
@Qualifier("documentController")
public class DocumentController implements UpdateController {
    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private StudentHelperBot studentHelperBot;

    @Override
    public void processUpdate(Update update) {
        Long chatId = update.getMessage().getChatId();
        States states = userStates.getOrDefault(chatId, States.ACTIVE);
        switch (states) {
            case ACTIVE -> producerProcess(update);
            case WAITING_FILE -> saveProcess(update);
            default -> log.error("Что-то пошло не так");
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

    private void producerProcess(Update update) {
        setView(messageUtils.generateSendMessageForDocument(update));
    }

    private void saveProcess(Update update) {
        Document document = update.getMessage().getDocument();

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(update.getMessage().getChatId());
        sendDocument.setDocument(new InputFile(document.getFileId()));

        try {
            studentHelperBot.execute(sendDocument);
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
        setUserStates(update, States.ACTIVE);
        log.info("Для пользователя {} установлено состояние {}",
                update.getMessage().getChat().getUserName(), States.ACTIVE);
    }
}
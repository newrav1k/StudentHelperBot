package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.enums.States;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
@Repository
@Qualifier("photoController")
public class PhotoController implements UpdateController {

    private StudentHelperBot studentHelperBot;

    @Override
    public void processUpdate(Update update) {
        long id = update.getMessage().getFrom().getId();
        States states = informationStorage.getState(id);
        try {
            switch (states) {
                case ACTIVE -> producerProcess(update);
                case WAITING_FILE_NAME_ADD -> setUserStates(update, States.WAITING_FILE_NAME_ADD);
                default -> log.info("Произошла непредвиденная ошибка!");
            }
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
    }

    @Override
    public void init(StudentHelperBot studentHelperBot) {
        this.studentHelperBot = studentHelperBot;
        log.info("Инициализация {} прошла успешно", this.getClass().getSimpleName());
    }

    @Override
    public void setView(SendMessage sendMessage) {
        studentHelperBot.sendAnswerMessage(sendMessage);
        log.info("Пользователю {} отправлено сообщение: {}", sendMessage.getChatId(), sendMessage.getText());
    }

    private void producerProcess(Update update) throws TelegramApiException {
        PhotoSize photoSize = update.getMessage().getPhoto().get(0);

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(update.getMessage().getChatId());
        sendPhoto.setPhoto(new InputFile(photoSize.getFileId()));

        studentHelperBot.execute(sendPhoto);
    }
}
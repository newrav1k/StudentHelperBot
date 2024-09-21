package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.enums.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Repository
@Qualifier("photoController")
public class PhotoController implements UpdateController {
    private static final Logger log = LoggerFactory.getLogger(PhotoController.class);

    private StudentHelperBot studentHelperBot;

    @Override
    public void processUpdate(Update update) {
        Long chatId = update.getMessage().getChatId();
        States states = userStates.getOrDefault(chatId, States.ACTIVE);
        switch (states) {
            case ACTIVE -> producerProcess(update);
            case WAITING_FILE -> {
                setUserStates(update, States.WAITING_FILE);
                log.info("Для пользователя {} установлено состояние {}",
                        update.getCallbackQuery().getFrom().getUserName(), States.WAITING_FILE);
            }
            default -> log.info("Произошла непредвиденная ошибка!");
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
        log.info("Запущен метод producerProcess для {}", update.getMessage().getChat().getUserName());
    }
}
package com.example.configuration;

import com.example.controller.StudentHelperBot;
import com.example.controller.type.CallbackDataController;
import com.example.controller.type.DocumentController;
import com.example.controller.type.PhotoController;
import com.example.controller.type.TextController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class StudentHelperBotConfiguration {
    @Bean
    public TelegramBotsApi telegramBotsApi(StudentHelperBot studentHelperBot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(studentHelperBot);
        } catch (TelegramApiException exception) {
            log.error("Ошибка при подключении бота");
        }
        return telegramBotsApi;
    }

    @Bean
    public CallbackDataController callbackDataController() {
        return new CallbackDataController();
    }

    @Bean
    public DocumentController documentController() {
        return new DocumentController();
    }

    @Bean
    public PhotoController photoController() {
        return new PhotoController();
    }

    @Bean
    public TextController textController() {
        return new TextController();
    }
}
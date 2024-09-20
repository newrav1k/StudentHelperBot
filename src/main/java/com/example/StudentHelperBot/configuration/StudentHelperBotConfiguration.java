package com.example.StudentHelperBot.configuration;

import com.example.StudentHelperBot.controller.StudentHelperBot;
import com.example.StudentHelperBot.controller.type.CallbackDataController;
import com.example.StudentHelperBot.controller.type.DocumentController;
import com.example.StudentHelperBot.controller.type.PhotoController;
import com.example.StudentHelperBot.controller.type.TextController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class StudentHelperBotConfiguration {
    private static final Logger log = LogManager.getLogger(StudentHelperBotConfiguration.class);

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
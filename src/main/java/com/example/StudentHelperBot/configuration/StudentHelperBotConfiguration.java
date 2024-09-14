package com.example.StudentHelperBot.configuration;

import com.example.StudentHelperBot.controller.StudentHelperBot;
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
}
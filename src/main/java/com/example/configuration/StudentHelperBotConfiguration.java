package com.example.configuration;

import com.example.controller.StudentHelperBot;
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
}
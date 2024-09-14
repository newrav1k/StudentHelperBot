package com.example.StudentHelperBot.controller;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class StudentHelperBot extends TelegramLongPollingBot {
    private static final Logger log = LogManager.getLogger(StudentHelperBot.class);

    public static final String START = "/start";
    public static final String UPLOAD_FILE = "/upload_file";
    public static final String HELP = "/help";

    @Value("${bot.name}")
    private String botName;

    private final UpdateController updateController;

    public StudentHelperBot(@Value("${bot.token}") String botToken, UpdateController updateController) {
        super(botToken);
        this.updateController = updateController;

        // работает, но бот сосёт огромный хуй Айдара
        List<BotCommand> botCommands = new ArrayList<>() {{
            add(new BotCommand(START, "Информация о боте"));
            add(new BotCommand(UPLOAD_FILE, "Загрузить файл"));
            add(new BotCommand(HELP, "Справка"));
        }};
        try {
            execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        updateController.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            if (data.equals("callback_data_save")) {
                log.info("callback_data_save");
            } else if (data.equals("callback_data_convert")) {
                log.info("callback_data_convert");
            }
        } else {
            updateController.processUpdate(update);
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    public void sendAnswerMessage(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException exception) {
                log.error("Не удалось отправить сообщение...{}", exception.getMessage());
            }
        }
    }
}
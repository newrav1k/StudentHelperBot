package com.example.controller;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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

    @Autowired
    private final List<UpdateController> updateController;
    private final ProcessController processController;

    public StudentHelperBot(@Value("${bot.token}") String botToken, List<UpdateController> updateController, ProcessController processController) {
        super(botToken);
        this.updateController = updateController;
        this.processController = processController;

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
        for (var controller : updateController) {
            controller.init(this);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        processController.processUpdate(update);
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
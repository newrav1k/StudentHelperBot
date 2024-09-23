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
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.example.controller.StudentHelperBot.*;

@Service
@Repository
@Qualifier("textController")
public class TextController implements UpdateController {
    private static final Logger log = LoggerFactory.getLogger(TextController.class);

    private StudentHelperBot studentHelperBot;

    @Override
    public void processUpdate(Update update) {
        Long chatId = update.getMessage().getChatId();
        String message = update.getMessage().getText();
        States states = userStates.getOrDefault(chatId, States.ACTIVE);
        switch (message) {
            case START -> setStartView(update);
            case HELP -> setHelpView(update);
            case RESET_STATE -> setUserStates(update, States.ACTIVE);
            case UPLOAD_FILE -> {
                setUploadFileView(update);
                setUserStates(update, States.WAITING_FILE);
            }
            case SHOW_DIRECTORIES -> setShowDirectoriesView(update);
            default -> {
                if (states != States.ACTIVE) {
                    if (states == States.WAITING_DIRECTORY_NAME_ADD) {
                        addDirectory(update, message);
                    } else if (states == States.WAITING_DIRECTORY_NAME_DELETE) {
                        deleteDirectory(update, message);
                    }
                } else {
                    producerProcess(update, message);
                }
            }
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

    private void setStartView(Update update) {
        Message message = update.getMessage();
        String text = String.format("""
                        Привет, %s! Я — бот, который с радостью сохранит ваши готовые работы на сервере, чтобы вы могли получить их в любой момент!
                        Кроме того, вы можете общаться со мной, как с ChatGPT.
                        
                        Чтобы узнать, какие функции уже доступны, введите команду /help.""",
                message.getChat().getFirstName());
        setView(messageUtils.generateSendMessageWithText(update, text));
    }

    private void setHelpView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
                """
                        ⚙️ Команды
                        
                        /start — описание и перезапуск бота
                        /upload_file — загрузка файла на сервер
                        /show_directories — отобразить все директории
                        /reset_state — сбросить состояние бота
                        """);
        setView(sendMessage);
    }

    private void setUploadFileView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageForDocumentSelection(update,
                "Загрузите файл, который хотите сохранить");
        setView(sendMessage);
    }

    private void setShowDirectoriesView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageForDirectories(update, directories);
        setView(sendMessage);
    }

    private void addDirectory(Update update, String message) {
        directories.add(message);
        log.info("Директория {} успешно добавлена", message);
        setUserStates(update, States.ACTIVE);
        setView(messageUtils.generateSendMessageWithText(update, "Директория успешно добавлена"));
    }

    private void deleteDirectory(Update update, String message) {
        boolean removed = directories.removeIf(s -> s.equals(message));
        if (removed) {
            log.info("Директория {} успешно удалена", message);
            setView(messageUtils.generateSendMessageWithText(update, "Директория успешно удалена"));
            setUserStates(update, States.ACTIVE);
        } else {
            setView(messageUtils.generateSendMessageWithText(update, "Вы указали неверное имя удаляемой директории"));
        }
    }

    private void producerProcess(Update update, String message) {
        setView(messageUtils.generateSendMessageWithText(update, message));
    }
}
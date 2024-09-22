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

import java.util.Iterator;

import static com.example.controller.StudentHelperBot.*;

@Service
@Repository
@Qualifier("textController")
public class TextController implements UpdateController {
    private static final Logger log = LoggerFactory.getLogger(TextController.class);

    private StudentHelperBot studentHelperBot;

    @Override
    public void processUpdate(Update update) {
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        States states = userStates.getOrDefault(chatId, States.ACTIVE);
        if (states != States.ACTIVE) {
            switch (states) {
                case WAITING_DIRECTORY_NAME_ADD -> addDirectory(update, message);
                case WAITING_DIRECTORY_NAME_DELETE -> deleteDirectory(update, message);
            }
        } else {
//        switch (States) // Проверять на состояние waiting_directory_name и при его наличии добавлять название в список
            // Само состояние задается в callback при нажатии соответственной кнопки
            switch (message) {
                case START -> setStartView(update);
                case HELP -> setHelpView(update);
                case UPLOAD_FILE -> {
                    setUploadFileView(update);
                    setUserStates(update, States.WAITING_FILE);
                    log.info("Для пользователя {} установлено состояние {}",
                            update.getMessage().getChat().getUserName(), States.WAITING_FILE);
                }
                case SHOW_DIRECTORIES -> setShowDirectoriesView(update);
                default -> setView(messageUtils.generateSendMessageWithText(update, message));
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
                        /show_directories — отобразить все директории""");
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
        setUserStates(update, States.ACTIVE);
        setView(messageUtils.generateSendMessageWithText(update, "Директория " + " успешно добавлена"));
        log.info("Директория {} успешно добавлена \n" +
                        "Для пользователя {} установлено состояние {}",
                message, update.getMessage().getChat().getUserName(), States.ACTIVE);
    }

    private void deleteDirectory(Update update, String message) {
        directories.removeIf(nextDirectory -> nextDirectory.equals(message));
        setUserStates(update, States.ACTIVE);
        setView(messageUtils.generateSendMessageWithText(update, "Директория " + " успешно удалена"));
        log.info("Директория {} успешно удалена \n" +
                        "Для пользователя {} установлено состояние {}",
                message, update.getMessage().getChat().getUserName(), States.ACTIVE);
    }
}
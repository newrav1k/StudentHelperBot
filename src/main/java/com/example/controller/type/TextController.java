package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import com.example.entity.Student;
import com.example.enums.States;
import com.example.exception.StudentHelperBotException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

import static com.example.controller.StudentHelperBot.DEVELOPERS;
import static com.example.controller.StudentHelperBot.HELP;
import static com.example.controller.StudentHelperBot.RESET_STATE;
import static com.example.controller.StudentHelperBot.SHOW_DIRECTORIES;
import static com.example.controller.StudentHelperBot.START;
import static com.example.controller.StudentHelperBot.UPLOAD_FILE;

@Slf4j
@Service
@Qualifier("textController")
public class TextController implements UpdateController {

    private StudentHelperBot studentHelperBot;

    private ApplicationContext context;

    @Override
    public void processUpdate(Update update) {
        long id = update.getMessage().getFrom().getId();
        String message = update.getMessage().getText();

        States states = informationStorage.getState(id);
        try {
//            Student student = studentDao.findById(update);
//            Update updateForDeleting = informationStorage.getUpdate(student.getId());         //Добавить реализацию добавдения последнего апдейта
//            deletingInlineKeyboardForCommand(updateForDeleting);
            switch (message) {
                case START -> {
                    setStartView(update);
                    studentDao.insert(update);
                }
                case HELP -> setHelpView(update);
                case RESET_STATE -> resetState(update, id);
                case "Конвертировать файл" -> convertFile(update);
                case UPLOAD_FILE, "Загрузить файл" -> processingFile(update);
                case SHOW_DIRECTORIES, "Отобразить директории" -> setShowDirectoriesView(update);
                case DEVELOPERS -> messageUtils.generateSendMessageAboutDevelopers(update).forEach(this::setView);
                default -> {
                    if (states != States.ACTIVE) {
                        switch (states) {
                            case CONVERT -> setView(messageUtils.generateSendMessageWithText(update,
                                    "Я не могу конвертировать текст! Отправьте документ"));
                            case WAITING_DIRECTORY_NAME_ADD -> addDirectory(update, message);
                            case WAITING_FILE_NAME -> renameFile(update, message);
                            case WAITING_DIRECTORY_NAME -> renameDirectory(update, message);
                            default -> producerProcess(update, message);
                        }
                    }
                }
            }
        } catch (StudentHelperBotException exception) {
            setView(messageUtils.generateSendMessageWithText(update, exception.getMessage()));
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
        log.info("Пользователю {} отправлено сообщение", sendMessage.getChatId());
    }

    private void setStartView(Update update) {
        Message message = update.getMessage();
        String text = String.format("""
                        Привет, %s!👋🏻 Я — бот, который с радостью сохранит ваши готовые работы на сервере, чтобы вы могли получить их в любой момент!⏳
                        Кроме того, вы можете общаться со мной, как с ChatGPT.💬
                        
                        Чтобы узнать, какие функции уже доступны, введите команду /help.📋""",
                message.getChat().getFirstName());
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update, text);
        sendMessage.setReplyMarkup(messageUtils.getMainMenuKeyboard());
        setView(sendMessage);
    }

    private void setHelpView(Update update) {
       SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
                """
                        ⚙️ Команды
                        
                        /start — описание и перезапуск бота 📌
                        /upload_file — загрузка файла на сервер 📌
                        /show_directories — отобразить все директории 📌
                        /reset_state — сбросить состояние бота 📌
                        """);
        setView(sendMessage);
    }

    private void resetState(Update update, Long id) {
        setUserStates(update, States.ACTIVE);
        informationStorage.clearData(id);
        setView(messageUtils.generateSendMessageWithText(update, "Состояние сброшено"));
    }

    private void processingFile(Update update) {
        processingFileView(update);
        setUserStates(update, States.WAITING_FILE_NAME_ADD);
    }

    private void processingFileView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageLookingForward(update,
                "Загрузите файл, который хотите сохранить");
        setView(sendMessage);
    }

    private void setShowDirectoriesView(Update update) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        setView(messageUtils.generateSendMessageForDirectories(update,
                directoryDao.findAll(student)));
    }

    private void convertFile(Update update) {
        setView(messageUtils.generateSendMessageWithText(update, "Загрузите файл, который хотите конвертировать:"));
        setUserStates(update, States.CONVERT);
    }

    private void addDirectory(Update update, String message) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        directoryDao.insert(student, message);
        log.info("Пользователь {} создал директорию", student.getId());
        setView(messageUtils.generateSendMessageWithText(update,
                "Директория успешно создана"));
        setUserStates(update, States.ACTIVE);
    }

    private void renameFile(Update update, String message) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        FileMetadata fileMetadata = informationStorage.getFileMetadata(student.getId());

        fileMetadataDao.renameFile(student, fileMetadata, message);

        setView(messageUtils.generateSendMessageWithText(update, "Новое имя установлено"));
        setUserStates(update, States.ACTIVE);
    }

    private void renameDirectory(Update update, String message) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = informationStorage.getDirectory(student.getId());

        directoryDao.renameDirectory(student, directory, message);

        setView(messageUtils.generateSendMessageWithText(update, "Новое имя установлено"));
        setUserStates(update, States.ACTIVE);
    }

    private void producerProcess(Update update, String message) {
        setView(messageUtils.generateSendMessageWithText(update, message));
    }

    private void deletingInlineKeyboardForCommand(Update update) {
        context.getBean(CallbackDataController.class).deleteInlineKeyboard(update);
    }

    @Autowired
    public void setContext(ApplicationContext context) {
        this.context = context;
    }
}
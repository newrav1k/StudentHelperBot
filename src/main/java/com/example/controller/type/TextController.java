package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.entity.Directory;
import com.example.enums.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

import static com.example.controller.StudentHelperBot.*;

@Service
@Repository
@Qualifier("textController")
public class TextController implements UpdateController {
    private static final Logger log = LoggerFactory.getLogger(TextController.class);

    private StudentHelperBot studentHelperBot;

    private static String directory;
    private static String file;
    private static final String DIRECTORY_ERROR = "Вы указали неверное имя директории";
    private static final String FILE_ERROR = "Вы указали неверное имя файла";

    @Override
    public void processUpdate(Update update) {
        Long chatId = update.getMessage().getChatId();
        String message = update.getMessage().getText();
        States states = userStates.getOrDefault(chatId, States.ACTIVE);
        switch (message) {
            case START -> {
                setStartView(update);
                studentDao.insert(update);
            }
            case HELP -> setHelpView(update);
            case RESET_STATE -> setUserStates(update, States.ACTIVE);
            case UPLOAD_FILE -> {
                setUploadFileView(update);
                setUserStates(update, States.WAITING_FILE_NAME_ADD);
            }
            case SHOW_DIRECTORIES -> setShowDirectoriesView(update);
            default -> {
                if (states != States.ACTIVE) {
                    switch (states) {
                        case WAITING_DIRECTORY_NAME_ADD -> addDirectory(update, message);
                        case WAITING_DIRECTORY_NAME_DELETE -> deleteDirectory(update, message);
                        case WAITING_DIRECTORY_NAME_CHOOSE -> setShowFilesView(update, message);
                        case WAITING_FILE_NAME_ADD -> addFile(update, message);
                        case WAITING_FILE_NAME_DOWNLOAD -> downloadFile(update, message);
                        case WAITING_FILE_NAME_DELETE -> deleteFile(update, message);
                        case WAITING_FILE_NAME_FOR_CHANGE -> chooseFileForChanging(update, message);
                        case WAITING_DIRECTORY_NAME_FOR_CHANGE -> chooseDirectoryForChange(update, message);
                        default -> producerProcess(update, message);
                    }
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
                        /reset_state — сбросить состояние бота
                        /show_directories — отобразить все директории
                        """);
        setView(sendMessage);
    }

    private void setUploadFileView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageForDocumentSelection(update,
                "Загрузите файл, который хотите сохранить");
        setView(sendMessage);
    }

    private void setShowDirectoriesView(Update update) {
        setView(messageUtils.generateSendMessageForDirectories(update,
                directoryDao.findAll(update)));
    }

    private void addDirectory(Update update, String message) {
        directoryDao.insert(update, message);
        setView(messageUtils.generateSendMessageWithText(update,
                "Директория успешно создана"));
        setUserStates(update, States.ACTIVE);
    }

    private void deleteDirectory(Update update, String message) {
        directoryDao.delete(update, message);
        setUserStates(update, States.ACTIVE);
    }

    private void setShowFilesView(Update update, String message) {
        Directory directory = directoryDao.findByTitle(update, message);
        directories.put(update.getMessage().getFrom().getId(), directory);

        setView(messageUtils.generateSendMessageForFiles(update,
                fileMetadataDao.findAll(update, directory)));
        setUserStates(update, States.ACTIVE);
    }

    private void addFile(Update update, String message) {
        boolean found = directoriesAndFiles.get(directory).contains(message);
        if (found) {
            setView(messageUtils.generateSendMessageWithText(update, "Такой файл уже существует"));
        } else {
            directoriesAndFiles.get(directory).add(message);
            log.info("Файл {} успешно добавлен", message);
            setUserStates(update, States.ACTIVE);
            setView(messageUtils.generateSendMessageWithText(update, "Файл успешно добавлен"));
        }
    }

    private void downloadFile(Update update, String message) {
        File file = fileMetadataDao.findById(update, directories.get(update.getMessage().getFrom().getId()), message);

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(update.getMessage().getChat().getId());
        sendDocument.setDocument(new InputFile(file));

        try {
            studentHelperBot.execute(sendDocument);
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        }
        setUserStates(update, States.ACTIVE);
    }

    private void deleteFile(Update update, String message) {
        boolean removed = directoriesAndFiles.get(directory).removeIf(s -> s.equals(message));
        if (removed) {
            log.info("Файл {} успешно удален", message);
            setView(messageUtils.generateSendMessageWithText(update, "Файл успешно удален"));
        } else {
            setView(messageUtils.generateSendMessageWithText(update, FILE_ERROR));
        }
        setUserStates(update, States.ACTIVE);
    }

    private void chooseFileForChanging(Update update, String message) {
        boolean found = directoriesAndFiles.get(directory).contains(message);
        if (found) {
            file = message;
            setUserStates(update, States.WAITING_DIRECTORY_NAME_FOR_CHANGE);
            setView(messageUtils.generateSendMessageWithText(update, "Введите название директории, в которую хотите переместить файл:"));
        } else {
            setView(messageUtils.generateSendMessageWithText(update, FILE_ERROR));
            setUserStates(update, States.ACTIVE);
        }
    }

    private void chooseDirectoryForChange(Update update, String message) {
        boolean found = directoriesAndFiles.containsKey(message);
        if (found) {
            directoriesAndFiles.get(message).add(file);
            directoriesAndFiles.get(directory).remove(file);
            setView(messageUtils.generateSendMessageWithText(update, "Файл успешно перемещен"));
        } else {
            setView(messageUtils.generateSendMessageWithText(update, DIRECTORY_ERROR));
        }
        setUserStates(update, States.ACTIVE);
    }

    private void producerProcess(Update update, String message) {
        setView(messageUtils.generateSendMessageWithText(update, message));
    }
}
package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import com.example.entity.Student;
import com.example.enums.States;
import com.example.exception.StudentHelperBotException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

import static com.example.controller.StudentHelperBot.HELP;
import static com.example.controller.StudentHelperBot.RESET_STATE;
import static com.example.controller.StudentHelperBot.SHOW_DIRECTORIES;
import static com.example.controller.StudentHelperBot.START;
import static com.example.controller.StudentHelperBot.UPLOAD_FILE;

@Slf4j
@Service
@Repository
@Qualifier("textController")
public class TextController implements UpdateController {

    private static final String[] STOP_WORDS = new String[]{"/stop", "stop", "стоп"};

    private StudentHelperBot studentHelperBot;

    @Override
    public void processUpdate(Update update) {
        long id = update.getMessage().getFrom().getId();
        String message = update.getMessage().getText();

        if (Arrays.asList(STOP_WORDS).contains(message)) {
            setUserStates(update, States.ACTIVE);
            return;
        }

        States states = informationStorage.getState(id);
        try {
            switch (message) {
                case START -> {
                    setStartView(update);
                    studentDao.insert(update);
                }
                case HELP -> setHelpView(update);
                case RESET_STATE -> resetState(update, id);
                case UPLOAD_FILE -> processingFile(update);
                case SHOW_DIRECTORIES -> setShowDirectoriesView(update);
                default -> {
                    if (states != States.ACTIVE) {
                        switch (states) {
                            case WAITING_DIRECTORY_NAME_ADD -> addDirectory(update, message);
                            case WAITING_DIRECTORY_NAME_DELETE -> deleteDirectory(update, message);
                            case WAITING_DIRECTORY_NAME_CHOOSE -> outputFiles(update, message);
                            case WAITING_FILE_NAME_DOWNLOAD -> uploadFile(update, message);
                            case WAITING_FILE_NAME_DELETE -> deleteFile(update, message);
                            case WAITING_FILE_NAME -> renameFile(update, message);
                            case WAITING_FILE_NAME_FOR_CHOOSE -> selectFileToMove(update, message);
                            case WAITING_FILE_NAME_FOR_CHANGE -> selectFileToRename(update, message);
                            case WAITING_DIRECTORY_NAME_FOR_CHANGE -> selectDirectoryToReceiveFile(update, message);
                            default -> producerProcess(update, message);
                        }
                    }
                }
            }
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
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
        setView(messageUtils.generateSendMessageWithText(update, text));
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

    private void addDirectory(Update update, String message) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        directoryDao.insert(student, message);
        log.info("Пользователь {} создал директорию", student.getId());
        setView(messageUtils.generateSendMessageWithText(update,
                "Директория успешно создана"));
        setUserStates(update, States.ACTIVE);
    }

    private void deleteDirectory(Update update, String message) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        if (StringUtils.isNumeric(message)) {
            directoryDao.deleteBySerial(student, Integer.parseInt(message));
        } else {
            directoryDao.deleteByTitle(student, message);
        }
        log.info("Пользователь {} удаляет директорию", student.getId());
        setView(messageUtils.generateSendMessageWithText(update, "Директория была успешно удалена"));
        setUserStates(update, States.ACTIVE);
    }

    private void outputFiles(Update update, String message) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = StringUtils.isNumeric(message) ?
                directoryDao.findBySerial(student, Integer.parseInt(message)) :
                directoryDao.findByTitle(student, message);
        informationStorage.putDirectory(student.getId(), directory);
        log.info("Обновлён последний документ у пользователя {}", student.getId());

        setView(messageUtils.generateSendMessageForFiles(update,
                fileMetadataDao.findAll(student, directory), directory));
        setUserStates(update, States.ACTIVE);
    }

    private void uploadFile(Update update, String message) throws TelegramApiException, StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = informationStorage.getDirectory(student.getId());
        FileMetadata fileMetadata = fileMetadataDao.findBySerial(student, directory, Integer.parseInt(message));

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(update.getMessage().getChat().getId());
        sendDocument.setDocument(new InputFile(FileMetadata.convertToInputStream(fileMetadata), fileMetadata.getTitle()));

        studentHelperBot.execute(sendDocument);

        setUserStates(update, States.ACTIVE);
    }

    private void deleteFile(Update update, String message) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = informationStorage.getDirectory(student.getId());

        fileMetadataDao.deleteBySerial(student, directory, Integer.parseInt(message));
        log.info("Пользователь {} удалил файл", student.getId());

        setView(messageUtils.generateSendMessageWithText(update, "Файл успешно удален"));
        setUserStates(update, States.ACTIVE);
    }

    private void selectFileToMove(Update update, String message) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = informationStorage.getDirectory(student.getId());
        FileMetadata fileMetadata = fileMetadataDao.findBySerial(student, directory, Integer.parseInt(message));

        informationStorage.putFileMetadata(student.getId(), fileMetadata);

        setView(messageUtils.generateSendMessageLookingForward(update, "Введите название директории, в которую хотите переместить файл:"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_FOR_CHANGE);
    }

    private void selectFileToRename(Update update, String message) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = informationStorage.getDirectory(student.getId());
        FileMetadata fileMetadata = fileMetadataDao.findBySerial(student, directory, Integer.parseInt(message));

        informationStorage.putFileMetadata(student.getId(), fileMetadata);

        setView(messageUtils.generateSendMessageLookingForward(update, "Введите новое имя для файла:"));
        setUserStates(update, States.WAITING_FILE_NAME);
    }

    private void renameFile(Update update, String message) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        FileMetadata fileMetadata = informationStorage.getFileMetadata(student.getId());

        fileMetadataDao.changeFileName(student, fileMetadata, message);

        setView(messageUtils.generateSendMessageWithText(update, "Новое имя установлено"));
        setUserStates(update, States.ACTIVE);
    }

    private void selectDirectoryToReceiveFile(Update update, String message) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = StringUtils.isNumeric(message) ?
                directoryDao.findBySerial(student, Integer.parseInt(message)) :
                directoryDao.findByTitle(student, message);
        fileMetadataDao.moveToDirectory(student, directory, informationStorage.getFileMetadata(student.getId()));

        setView(messageUtils.generateSendMessageWithText(update, "Файл успешно перемещен"));
        setUserStates(update, States.ACTIVE);
    }

    private void processingFile(Update update) {
        processingFileView(update);
        setUserStates(update, States.WAITING_FILE_NAME_ADD);
    }

    private void resetState(Update update, Long id) {
        setUserStates(update, States.ACTIVE);
        informationStorage.clearData(id);
        setView(messageUtils.generateSendMessageWithText(update, "Состояние сброшено"));
    }

    private void producerProcess(Update update, String message) {
        setView(messageUtils.generateSendMessageWithText(update, message));
    }
}
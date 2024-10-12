package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import com.example.entity.Student;
import com.example.enums.CallbackData;
import com.example.enums.States;
import com.example.exception.StudentHelperBotException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Service
@Repository
@Qualifier("callbackDataController")
public class CallbackDataController implements UpdateController {

    @Setter
    private static String inlineKeyboardText;

    private StudentHelperBot studentHelperBot;

    private ApplicationContext context;

    @Async
    @Override
    public void processUpdate(Update update) {
        long id = update.getCallbackQuery().getFrom().getId();
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();
        deleteInlineKeyboard(update);
        States states = informationStorage.getState(id);
        try {
            switch (states) {
                case WAITING_DIRECTORY_NAME_CHOOSE_IMPLEMENTATION -> chooseDirectory(update);
                case WAITING_DIRECTORY_NAME_DELETE_IMPLEMENTATION -> deleteDirectoryConfirmationOrCancel(update);
                case WAITING_FILE_NAME_DOWNLOAD_IMPLEMENTATION -> downloadFile(update);
                case WAITING_FILE_NAME_DELETE_IMPLEMENTATION -> deleteFileConfirmation(update);
                case WAITING_FILE_NAME_FOR_CHANGE_IMPLEMENTATION -> renameFile(update);
                case WAITING_FILE_NAME_FOR_MOVING_IMPLEMENTATION -> selectDirectoryForMoving(update);
                case WAITING_DIRECTORY_NAME_FOR_MOVING_IMPLEMENTATION -> fileMoving(update);
                default -> {
                    if (data != null) {
                        switch (CallbackData.fromString(data)) {
                            case CALLBACK_DATA_SAVE -> saveProcess(update);
                            case CALLBACK_DATA_CONVERT -> convertProcess(update);
                            case CALLBACK_DATA_DELETE_DIRECTORY -> deleteDirectoryProcess(update);
                            case CALLBACK_DATA_CANCEL -> cancelProcess(update);
                            case CALLBACK_DATA_ADD_DIRECTORY -> addDirectoryProcess(update);
                            case CALLBACK_DATA_CHOOSE_DIRECTORY -> chooseDirectoryProcess(update);
                            case CALLBACK_DATA_ADD_FILE -> addFileProcess(update);
                            case CALLBACK_DATA_DOWNLOAD_FILE -> downloadFileProcess(update);
                            case CALLBACK_DATA_DELETE_FILE -> deleteFileProcess(update);
                            case CALLBACK_DATA_CHANGE_FILE_DIRECTORY -> selectFileForMovingProcess(update);
                            case CALLBACK_DATA_CHANGE_FILE_NAME -> renameFileProcess(update);
                            case CALLBACK_DATA_CANCEL_FILE -> cancelFromFilesListProcess(update);
                            case CALLBACK_DATA_DIRECTORY_CONFIRMATION_YES -> deleteDirectory(update);
                            case CALLBACK_DATA_DIRECTORY_CONFIRMATION_NO -> cancelDeletionDirectory(update);
                        }
                    }
                }
            }
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
        } catch (StudentHelperBotException exception) {
            setView(messageUtils.generateSendMessageWithCallbackData(update, exception.getMessage()));
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
        log.info("Пользователю {} отправлено сообщение: {}", sendMessage.getChatId(), sendMessage.getText());
    }

    private void saveProcess(Update update) throws TelegramApiException, StudentHelperBotException {
        Student student = studentDao.findById(update);
        File previousFile = informationStorage.getTGFile(student.getId());
        java.io.File file = studentHelperBot.downloadFile(previousFile);

        Document document = informationStorage.getDocument(student.getId());

        fileMetadataDao.insert(update, null, file, document);

        setView(messageUtils.generateSendMessageWithCallbackData(update,
                "Файл успешно сохранён"));
    }

    private void convertProcess(Update update) {
        try {
            context.getBean(DocumentController.class).converter(update);
        } catch (TelegramApiException | IOException exception) {
            log.error(exception.getMessage());
        } catch (StudentHelperBotException exception) {
            setView(messageUtils.generateSendMessageWithCallbackData(update, exception.getMessage()));
        }
    }

    private void cancelProcess(Update update) {
        setUserStates(update, States.ACTIVE);
    }

    private void addDirectoryProcess(Update update) {
        setView(messageUtils.generateSendMessageLookingForward(update, "Введите название новой директории:"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_ADD);
    }

    private void chooseDirectoryProcess(Update update) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        studentHelperBot.sendEditMessage(messageUtils.editDirectoriesMessageWithChooseButtons(update,
                "Выберете директорию, в которую хотите перейти:", directoryDao.findAll(student), "choose"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_CHOOSE_IMPLEMENTATION);
    }

    private void chooseDirectory(Update update) throws StudentHelperBotException {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_directories_action")) {
            cancelFromDirectoriesChooseAction(update);
        } else {
            Student student = studentDao.findById(update);
            Directory directory = directoryDao.findByTitle(student, update.getCallbackQuery().getData().split("_")[3]);
            informationStorage.putDirectory(student.getId(), directory);
            studentHelperBot.sendEditMessage(messageUtils.editSendMessageForFiles(update,
                    fileMetadataDao.findAll(student, directory), directory));
        }
        setUserStates(update, States.ACTIVE);
    }

    private void deleteDirectoryProcess(Update update) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        studentHelperBot.sendEditMessage(messageUtils.editDirectoriesMessageWithChooseButtons(update,
                "Выберете директорию, которую хотите удалить:", directoryDao.findAll(student), "delete"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_DELETE_IMPLEMENTATION);
    }

    private void deleteDirectoryConfirmationOrCancel(Update update) throws StudentHelperBotException {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_directories_action")) {
            cancelFromDirectoriesChooseAction(update);
        } else {
            Student student = studentDao.findById(update);
            Directory directory = directoryDao.findByTitle(student, update.getCallbackQuery().getData().split("_")[3]);
            informationStorage.putDirectory(student.getId(), directory);
            studentHelperBot.sendEditMessage(messageUtils.directoryDeletionConfirmation(update, "Вы действительно хотите удалить директорию ", directory));
        }
        setUserStates(update, States.ACTIVE);
    }

    private void deleteDirectory(Update update) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = informationStorage.getDirectory(student.getId());

        directoryDao.deleteByTitle(student, directory.getTitle());
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Директория успешно удалена"));
    }

    private void cancelDeletionDirectory(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Удаление директории прекращено"));
        setUserStates(update, States.ACTIVE);
    }

    private void addFileProcess(Update update) {
        setView(messageUtils.generateSendMessageLookingForward(update, "Загрузите файл, который хотите добавить:"));
        setUserStates(update, States.WAITING_FILE_NAME_ADD);
    }

    private void downloadFileProcess(Update update) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = informationStorage.getDirectory(student.getId());
        studentHelperBot.sendEditMessage(messageUtils.editFilesMessageWithChooseButtons(update,
                "Выберете файл, который хотите скачать:",
                fileMetadataDao.findAll(student, directory),
                "download"));
        setUserStates(update, States.WAITING_FILE_NAME_DOWNLOAD_IMPLEMENTATION);
    }

    private void downloadFile(Update update) throws StudentHelperBotException, TelegramApiException {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_files_action")) {
            cancelFromFilesChooseAction(update);
        } else {
            Student student = studentDao.findById(update);
            Directory directory = informationStorage.getDirectory(student.getId());
            FileMetadata fileMetadata = fileMetadataDao.findBySerial(student, directory, Integer.parseInt(update.getCallbackQuery().getData().split("_")[3]));

            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(update.getCallbackQuery().getFrom().getId());
            sendDocument.setDocument(new InputFile(FileMetadata.convertToInputStream(fileMetadata), fileMetadata.getTitle()));

            studentHelperBot.execute(sendDocument);
        }

        setUserStates(update, States.ACTIVE);
    }

    private void deleteFileProcess(Update update) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = informationStorage.getDirectory(student.getId());
        studentHelperBot.sendEditMessage(messageUtils.editFilesMessageWithChooseButtons(update,
                "Выберете файл, который хотите удалить:",
                fileMetadataDao.findAll(student, directory),
                "delete"));
        setUserStates(update, States.WAITING_FILE_NAME_DELETE_IMPLEMENTATION);
    }

    private void deleteFileConfirmation(Update update) throws StudentHelperBotException {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_files_action")) {
            cancelFromFilesChooseAction(update);
        } else {
            Student student = studentDao.findById(update);
            Directory directory = informationStorage.getDirectory(student.getId());
            // Расскомментировать, когда появиться удаление файла через title
//        FileMetadata fileMetadata = fileMetadataDao.findBySerial(student, directory, Integer.parseInt(update.getCallbackQuery().getData().split("_")[3]));
//
//        informationStorage.putFileMetadata(student.getId(), fileMetadata);
//
//        studentHelperBot.sendEditMessage(messageUtils.fileDeletionConfirmation(update, "Вы действительно хотите удалить файл ", fileMetadata));

            fileMetadataDao.deleteBySerial(student, directory, Integer.parseInt(update.getCallbackQuery().getData().split("_")[3]));

            log.info("Пользователь {} удалил файл", student.getId());

            setView(messageUtils.generateSendMessageWithCallbackData(update, "Файл успешно удален"));
        }

        setUserStates(update, States.ACTIVE);

        }

    // Расскомментировать, когда появиться удаление файла через title
//    private void deleteFile(Update update) throws StudentHelperBotException {
//        Student student = studentDao.findById(update);
//        FileMetadata fileMetadata  = informationStorage.getFileMetadata(student.getId());
//
//        fileMetadataDao.deleteByTitle(student, directory, fileMetadata.getTitle());
//        setView(messageUtils.generateSendMessageWithCallbackData(update, "Файл успешно удален"));
//    }

    private void selectFileForMovingProcess(Update update) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = informationStorage.getDirectory(student.getId());
        studentHelperBot.sendEditMessage(messageUtils.editFilesMessageWithChooseButtons(update,
                "Выберите файл, который хотите перенести в другую директорию:",
                fileMetadataDao.findAll(student, directory),
                "move"));
        setUserStates(update, States.WAITING_FILE_NAME_FOR_MOVING_IMPLEMENTATION);
    }

    private void selectDirectoryForMoving(Update update) throws StudentHelperBotException {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_files_action")) {
            cancelFromFilesChooseAction(update);
            setUserStates(update, States.ACTIVE);
        } else {
            Student student = studentDao.findById(update);
            Directory directory = informationStorage.getDirectory(student.getId());
            FileMetadata fileMetadata = fileMetadataDao.findBySerial(student, directory, Integer.parseInt(update.getCallbackQuery().getData().split("_")[3]));

            informationStorage.putFileMetadata(student.getId(), fileMetadata);

            studentHelperBot.sendEditMessage(messageUtils.editDirectoriesMessageWithChooseButtons(update,
                    "Выберете директорию, в которую хотите переместить файл:", directoryDao.findAll(student), "move"));
            setUserStates(update, States.WAITING_DIRECTORY_NAME_FOR_MOVING_IMPLEMENTATION);
        }
    }

    private void fileMoving(Update update) throws StudentHelperBotException {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_directories_action")) {
            cancelFromFilesChooseAction(update);
        } else {
            Student student = studentDao.findById(update);
            Directory directory = directoryDao.findByTitle(student, update.getCallbackQuery().getData().split("_")[3]);
            fileMetadataDao.moveToDirectory(student, directory, informationStorage.getFileMetadata(student.getId()));

            setView(messageUtils.generateSendMessageWithCallbackData(update, "Файл успешно перемещен"));
        }

        setUserStates(update, States.ACTIVE);
    }

    private void renameFileProcess(Update update) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = informationStorage.getDirectory(student.getId());
        studentHelperBot.sendEditMessage(messageUtils.editFilesMessageWithChooseButtons(update,
                "Выберете файл, который хотите переименовать:",
                fileMetadataDao.findAll(student, directory),
                "rename"));
        setUserStates(update, States.WAITING_FILE_NAME_FOR_CHANGE_IMPLEMENTATION);
    }

    private void renameFile(Update update) throws StudentHelperBotException {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_files_action")) {
            cancelFromFilesChooseAction(update);
            setUserStates(update, States.ACTIVE);
        } else {
            Student student = studentDao.findById(update);
            Directory directory = informationStorage.getDirectory(student.getId());
            FileMetadata fileMetadata = fileMetadataDao.findBySerial(student, directory, Integer.parseInt(update.getCallbackQuery().getData().split("_")[3]));

            informationStorage.putFileMetadata(student.getId(), fileMetadata);

            setView(messageUtils.generateSendMessageLookingForward(update, "Введите новое имя для файла:"));
            setUserStates(update, States.WAITING_FILE_NAME);
        }
    }

    private void cancelFromFilesListProcess(Update update) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        studentHelperBot.sendEditMessage(messageUtils.editSendMessageForDirectories(update, directoryDao.findAll(student)));
        setUserStates(update, States.ACTIVE);
    }

    private void cancelFromDirectoriesChooseAction(Update update) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        studentHelperBot.sendEditMessage(messageUtils.editSendMessageForDirectories(update, directoryDao.findAll(student)));
    }

    private void cancelFromFilesChooseAction(Update update) throws StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = informationStorage.getDirectory(student.getId());
        studentHelperBot.sendEditMessage(messageUtils.editSendMessageForFiles(update,
                fileMetadataDao.findAll(student, directory), directory));
    }

    private void deleteInlineKeyboard(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String text = inlineKeyboardText;
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setReplyMarkup(null);
        studentHelperBot.sendEditMessage(editMessage);
    }

    @Autowired
    public void setContext(ApplicationContext context) {
        this.context = context;
    }
}
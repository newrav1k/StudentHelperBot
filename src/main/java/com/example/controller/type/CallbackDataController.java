package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import com.example.entity.Student;
import com.example.enums.CallbackData;
import com.example.enums.States;
import com.example.exception.StudentHelperBotException;
import com.example.service.DirectoryService;
import com.example.service.FileService;
import com.example.service.StudentService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
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
@Qualifier("callbackDataController")
public class CallbackDataController implements UpdateController {

    @Setter
    private static String inlineKeyboardText;

    private StudentHelperBot studentHelperBot;

    private ApplicationContext context;

    private final StudentService studentService;

    private final DirectoryService directoryService;

    private final FileService fileService;

    @Autowired
    public CallbackDataController(StudentService studentService, DirectoryService directoryService, FileService fileService) {
        this.studentService = studentService;
        this.directoryService = directoryService;
        this.fileService = fileService;
    }

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
                case WAITING_DIRECTORY_NAME_FOR_CHANGE_IMPLEMENTATION -> renameDirectory(update);
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
                            case CALLBACK_DATA_CHANGE_DIRECTORY_NAME -> renameDirectoryProcess(update);
                            case CALLBACK_DATA_CHANGE_FILE_NAME -> renameFileProcess(update);
                            case CALLBACK_DATA_CANCEL_FILE -> cancelFromFilesListProcess(update);
                            case CALLBACK_DATA_CONFIRMATION_YES_DIRECTORY -> deleteDirectory(update);
                            case CALLBACK_DATA_CONFIRMATION_NO_DIRECTORY -> cancelDeletionDirectory(update);
                            case CALLBACK_DATA_CONFIRMATION_YES_FILE -> deleteFile(update);
                            case CALLBACK_DATA_CONFIRMATION_NO_FILE -> cancelDeletionFile(update);
                            default -> log.error("Invalid callback query state");
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
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        File previousFile = informationStorage.getTGFile(student.getId());
        java.io.File file = studentHelperBot.downloadFile(previousFile);

        Document document = informationStorage.getDocument(student.getId());

        fileService.save(update, null, file, document);
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

    private void chooseDirectoryProcess(Update update) {
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        studentHelperBot.sendEditMessage(messageUtils.editDirectoriesMessageWithChooseButtons(update,
                "Выберете директорию, в которую хотите перейти:", directoryService.findAll(student), "choose"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_CHOOSE_IMPLEMENTATION);
    }

    private void chooseDirectory(Update update) {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_directories_action")) {
            cancelFromDirectoriesChooseAction(update);
        } else {
            Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
            Directory directory = directoryService.findByTitle(student, update.getCallbackQuery().getData().split("_")[3]);
            informationStorage.putDirectory(student.getId(), directory);
            studentHelperBot.sendEditMessage(messageUtils.editSendMessageForFiles(update,
                    fileService.findAll(directory), directory));
        }
        setUserStates(update, States.ACTIVE);
    }

    private void renameDirectoryProcess(Update update) {
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        studentHelperBot.sendEditMessage(messageUtils.editDirectoriesMessageWithChooseButtons(update,
                "Выберете директорию, которую хотите переименовать:", directoryService.findAll(student), "rename"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_FOR_CHANGE_IMPLEMENTATION);
    }

    private void renameDirectory(Update update) {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_directories_action")) {
            cancelFromDirectoriesChooseAction(update);
        } else {
            Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
            Directory directory = directoryService.findByTitle(student, update.getCallbackQuery().getData().split("_")[3]);

            informationStorage.putDirectory(student.getId(), directory);

            setView(messageUtils.generateSendMessageLookingForward(update, "Введите новое имя для директории:"));
            setUserStates(update, States.WAITING_DIRECTORY_NAME);
        }
    }

    private void deleteDirectoryProcess(Update update) {
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        studentHelperBot.sendEditMessage(messageUtils.editDirectoriesMessageWithChooseButtons(update,
                "Выберете директорию, которую хотите удалить:", directoryService.findAll(student), "delete"));
        setUserStates(update, States.WAITING_DIRECTORY_NAME_DELETE_IMPLEMENTATION);
    }

    private void deleteDirectoryConfirmationOrCancel(Update update) {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_directories_action")) {
            cancelFromDirectoriesChooseAction(update);
        } else {
            Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
            Directory directory = directoryService.findByTitle(student, update.getCallbackQuery().getData().split("_")[3]);
            informationStorage.putDirectory(student.getId(), directory);
            studentHelperBot.sendEditMessage(messageUtils.directoryDeletionConfirmation(update, "Вы действительно хотите удалить директорию ", directory));
        }
        setUserStates(update, States.ACTIVE);
    }

    private void deleteDirectory(Update update) {
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        Directory directory = informationStorage.getDirectory(student.getId());

        directoryService.deleteByTitle(student, directory.getTitle());

        SendMessage sendMessage = messageUtils.generateSendMessageWithCallbackData(update, "Директория успешно удалена");
        sendMessage.setReplyMarkup(messageUtils.getMainMenuKeyboard());
        setView(sendMessage);
    }

    private void cancelDeletionDirectory(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Удаление директории прекращено"));
    }

    private void addFileProcess(Update update) {
        setView(messageUtils.generateSendMessageLookingForward(update, "Загрузите файл, который хотите добавить:"));
        setUserStates(update, States.WAITING_FILE_NAME_ADD);
    }

    private void downloadFileProcess(Update update) {
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        Directory directory = informationStorage.getDirectory(student.getId());
        studentHelperBot.sendEditMessage(messageUtils.editFilesMessageWithChooseButtons(update,
                "Выберете файл, который хотите скачать:",
                fileService.findAll(directory),
                "download"));
        setUserStates(update, States.WAITING_FILE_NAME_DOWNLOAD_IMPLEMENTATION);
    }

    private void downloadFile(Update update) throws TelegramApiException {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_files_action")) {
            cancelFromFilesChooseAction(update);
        } else {
            Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
            Directory directory = informationStorage.getDirectory(student.getId());
            FileMetadata fileMetadata = fileService.findBySerial(directory, Integer.parseInt(update.getCallbackQuery().getData().split("_")[3]));

            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(update.getCallbackQuery().getFrom().getId());
            sendDocument.setDocument(new InputFile(FileMetadata.convertToInputStream(fileMetadata), fileMetadata.getTitle()));

            studentHelperBot.execute(sendDocument);
        }

        setUserStates(update, States.ACTIVE);
    }

    private void deleteFileProcess(Update update) {
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        Directory directory = informationStorage.getDirectory(student.getId());
        studentHelperBot.sendEditMessage(messageUtils.editFilesMessageWithChooseButtons(update,
                "Выберете файл, который хотите удалить:",
                fileService.findAll(directory),
                "delete"));
        setUserStates(update, States.WAITING_FILE_NAME_DELETE_IMPLEMENTATION);
    }

    private void deleteFileConfirmation(Update update) {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_files_action")) {
            cancelFromFilesChooseAction(update);
        } else {
            Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
            Directory directory = informationStorage.getDirectory(student.getId());

            FileMetadata fileMetadata = fileService.findBySerial(directory, Integer.parseInt(update.getCallbackQuery().getData().split("_")[3]));

            informationStorage.putFileMetadata(student.getId(), fileMetadata);

            studentHelperBot.sendEditMessage(messageUtils.fileDeletionConfirmation(update, "Вы действительно хотите удалить файл ", fileMetadata));
        }

        setUserStates(update, States.ACTIVE);
    }

    private void deleteFile(Update update) {
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        FileMetadata fileMetadata = informationStorage.getFileMetadata(student.getId());
        Directory directory = informationStorage.getDirectory(student.getId());

        fileService.deleteByTitle(directory, fileMetadata.getTitle());

        setView(messageUtils.generateSendMessageWithCallbackData(update, "Файл успешно удален"));
    }

    private void cancelDeletionFile(Update update) {
        setView(messageUtils.generateSendMessageWithCallbackData(update, "Удаление файла прекращено"));
    }

    private void selectFileForMovingProcess(Update update) {
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        Directory directory = informationStorage.getDirectory(student.getId());
        studentHelperBot.sendEditMessage(messageUtils.editFilesMessageWithChooseButtons(update,
                "Выберите файл, который хотите перенести в другую директорию:",
                fileService.findAll(directory),
                "move"));
        setUserStates(update, States.WAITING_FILE_NAME_FOR_MOVING_IMPLEMENTATION);
    }

    private void selectDirectoryForMoving(Update update) {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_files_action")) {
            cancelFromFilesChooseAction(update);
            setUserStates(update, States.ACTIVE);
        } else {
            Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
            Directory directory = informationStorage.getDirectory(student.getId());
            FileMetadata fileMetadata = fileService.findBySerial(directory,
                    Integer.parseInt(update.getCallbackQuery().getData().split("_")[3]));

            informationStorage.putFileMetadata(student.getId(), fileMetadata);

            studentHelperBot.sendEditMessage(messageUtils.editDirectoriesMessageWithChooseButtons(update,
                    "Выберете директорию, в которую хотите переместить файл:", directoryService.findAll(student), "move"));
            setUserStates(update, States.WAITING_DIRECTORY_NAME_FOR_MOVING_IMPLEMENTATION);
        }
    }

    private void fileMoving(Update update) {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_directories_action")) {
            cancelFromFilesChooseAction(update);
        } else {
            Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
            Directory directory = directoryService.findByTitle(student, update.getCallbackQuery().getData().split("_")[3]);
//            fileService.moveToDirectory(student, directory, informationStorage.getFileMetadata(student.getId()));
            fileService.moveToDirectory(directory, informationStorage.getFileMetadata(student.getId()));

            setView(messageUtils.generateSendMessageWithCallbackData(update, "Файл успешно перемещен"));
        }

        setUserStates(update, States.ACTIVE);
    }

    private void renameFileProcess(Update update) {
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        Directory directory = informationStorage.getDirectory(student.getId());
        studentHelperBot.sendEditMessage(messageUtils.editFilesMessageWithChooseButtons(update,
                "Выберете файл, который хотите переименовать:",
                fileService.findAll(directory),
                "rename"));
        setUserStates(update, States.WAITING_FILE_NAME_FOR_CHANGE_IMPLEMENTATION);
    }

    private void renameFile(Update update) {
        if (Objects.equals(update.getCallbackQuery().getData(), "callback_data_cancel_for_files_action")) {
            cancelFromFilesChooseAction(update);
            setUserStates(update, States.ACTIVE);
        } else {
            Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
            Directory directory = informationStorage.getDirectory(student.getId());
            FileMetadata fileMetadata = fileService.findBySerial(directory, Integer.parseInt(update.getCallbackQuery().getData().split("_")[3]));

            informationStorage.putFileMetadata(student.getId(), fileMetadata);

            setView(messageUtils.generateSendMessageLookingForward(update, "Введите новое имя для файла:"));
            setUserStates(update, States.WAITING_FILE_NAME);
        }
    }

    private void cancelFromFilesListProcess(Update update) {
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        studentHelperBot.sendEditMessage(messageUtils.editSendMessageForDirectories(update, directoryService.findAll(student)));
        setUserStates(update, States.ACTIVE);
    }

    private void cancelFromDirectoriesChooseAction(Update update) {
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        studentHelperBot.sendEditMessage(messageUtils.editSendMessageForDirectories(update, directoryService.findAll(student)));
    }

    private void cancelFromFilesChooseAction(Update update) {
        Student student = studentService.findById(update.getCallbackQuery().getFrom().getId());
        Directory directory = informationStorage.getDirectory(student.getId());
        studentHelperBot.sendEditMessage(messageUtils.editSendMessageForFiles(update,
                fileService.findAll(directory), directory));
    }

    public void deleteInlineKeyboard(Update update) {
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
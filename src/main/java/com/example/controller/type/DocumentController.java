package com.example.controller.type;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.entity.Directory;
import com.example.entity.Student;
import com.example.enums.FileType;
import com.example.enums.States;
import com.example.exception.StudentHelperBotException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Service
@Repository
@Qualifier("documentController")
public class DocumentController implements UpdateController {

    private StudentHelperBot studentHelperBot;

    @Async
    @Override
    public void processUpdate(Update update) {
        long id = update.getMessage().getFrom().getId();
        States states = informationStorage.getState(id);

        File file;
        Document document = update.getMessage().getDocument();
        try {
            file = studentHelperBot.execute(new GetFile(document.getFileId()));
            informationStorage.putDocument(id, document);
            informationStorage.putTGFile(id, file);
            switch (states) {
                case ACTIVE -> producerProcess(update);
                case CONVERT -> converter(update);
                case WAITING_FILE_NAME_ADD -> saveProcess(update);
                default -> log.error("Что-то пошло не так");
            }
        } catch (TelegramApiException | IOException exception) {
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
        log.info("Пользователю {} отправлено сообщение: {}", sendMessage.getChatId(), sendMessage.getText());
    }

    private void producerProcess(Update update) {
        setView(messageUtils.generateSendMessageForDocument(update));
    }

    public void converter(Update update) throws TelegramApiException, IOException, StudentHelperBotException {
        Student student = studentDao.findById(update);
        Document document = informationStorage.getDocument(student.getId());
        File execute = studentHelperBot.execute(new GetFile(document.getFileId()));

        FileType fileType = FileType.fromString(document.getFileName().split("\\.")[1]);

        if (fileType == null) {
            throw new StudentHelperBotException("Данный тип файла я не могу конвертировать");
        }
        setView(update.hasCallbackQuery() ?
                messageUtils.generateSendMessageWithCallbackData(update, "Конвертируем файл...") :
                messageUtils.generateSendMessageWithText(update, "Конвертируем файл..."));

        java.io.File file = studentHelperBot.downloadFile(execute);
        java.io.File pdfFile = Files.createTempFile(document.getFileName().split("\\.")[0], ".pdf").toFile();

        IConverter converter = LocalConverter.builder().build();

        converter.convert(file).as(fileType.getDocumentType())
                .to(pdfFile).as(DocumentType.PDF)
                .execute();

        converter.shutDown();

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(student.getId());
        sendDocument.setDocument(new InputFile(pdfFile));

        studentHelperBot.execute(sendDocument);

        pdfFile.deleteOnExit();
    }

    private void saveProcess(Update update) throws TelegramApiException, StudentHelperBotException {
        Student student = studentDao.findById(update);
        Directory directory = informationStorage.getDirectory(student.getId());
        Document document = update.getMessage().getDocument();

        File execute = studentHelperBot.execute(new GetFile(document.getFileId()));
        java.io.File downloadFile = studentHelperBot.downloadFile(execute);

        fileMetadataDao.insert(update, directory, downloadFile, document);
        setView(messageUtils.generateSendMessageWithText(update,
                "Файл успешно сохранён"));
        setUserStates(update, States.ACTIVE);
    }
}
package com.example.controller.type;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.entity.Student;
import com.example.enums.FileType;
import com.example.enums.States;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
                case WAITING_FILE_NAME_ADD -> saveProcess(update);
                default -> log.error("Что-то пошло не так");
            }
        } catch (TelegramApiException exception) {
            log.error(exception.getMessage());
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

    private void producerProcess(Update update) {
        setView(messageUtils.generateSendMessageForDocument(update));
    }

    public void converter(Update update) throws TelegramApiException, IOException {
        Document document = informationStorage.getDocument(update.getCallbackQuery().getFrom().getId());
        File execute = studentHelperBot.execute(new GetFile(document.getFileId()));

        java.io.File wordFile = studentHelperBot.downloadFile(execute);
        java.io.File pdfFile = Files.createTempFile(document.getFileName().split("\\.")[0], ".pdf").toFile();

        IConverter converter = LocalConverter.builder().build();

        FileType fileType = FileType.fromString(document.getFileName().split("\\.")[1]);

        converter.convert(wordFile).as(fileType.getDocumentType())
                .to(pdfFile).as(DocumentType.PDF)
                .execute();

        converter.shutDown();

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(update.getCallbackQuery().getFrom().getId());
        sendDocument.setDocument(new InputFile(pdfFile));
        studentHelperBot.execute(sendDocument);

        pdfFile.deleteOnExit();
    }

    private void saveProcess(Update update) throws TelegramApiException {
        Student student = studentDao.findById(update);
        Document document = update.getMessage().getDocument();

        File execute = studentHelperBot.execute(new GetFile(document.getFileId()));
        java.io.File downloadFile = studentHelperBot.downloadFile(execute);

        fileMetadataDao.insert(update, informationStorage.getDirectory(student.getId()), downloadFile, document);
        setView(messageUtils.generateSendMessageWithText(update,
                "Файл успешно сохранён"));
        setUserStates(update, States.ACTIVE);
    }
}
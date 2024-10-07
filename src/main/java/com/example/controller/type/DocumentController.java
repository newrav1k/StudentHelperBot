package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
import com.example.enums.FileType;
import com.example.enums.States;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
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
import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

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
            informationStorage.putFile(id, file);
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
//        long id = update.getCallbackQuery().getFrom().getId();
//        // Получаем объект документа из сообщения
//        String fileId = informationStorage.getDocument(id).getFileId();
//
//        // Скачиваем документ Word
//        InputStream inputStream = downloadFileAsStream(fileId);
//
//        // Конвертируем документ в PDF
//        ByteArrayOutputStream pdfOutputStream = convertWordToPdf(inputStream);
//
//        // Отправляем PDF обратно пользователю
//        sendPdfDocument(String.valueOf(id), pdfOutputStream, informationStorage.getDocument(id).getFileName(), update);

//        Document document = informationStorage.getDocument(update.getCallbackQuery().getFrom().getId());
//        Path path = Files.createTempFile(document.getFileName().split("\\.")[0], ".pdf");
//        java.io.File file = path.toFile();
//        File execute = studentHelperBot.execute(new GetFile(document.getFileId()));
//        try (FileOutputStream fos = new FileOutputStream(file)) {
//            fos.write(Files.readAllBytes(Path.of(studentHelperBot.downloadFile(execute).toURI())));
//        } catch (TelegramApiException ex) {
//            log.error("Error of convertering");
//        }
//        SendDocument sendDocument = new SendDocument();
//        sendDocument.setChatId(update.getCallbackQuery().getFrom().getId());
//        sendDocument.setDocument(new InputFile(file));
//        studentHelperBot.execute(sendDocument);


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

    private InputStream downloadFileAsStream(String fileId) throws TelegramApiException, IOException {
        // Получаем объект файла по fileId
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(fileId);

        // Загружаем файл через API
        org.telegram.telegrambots.meta.api.objects.File telegramFile = studentHelperBot.execute(getFileMethod);

        // Строим URL для скачивания файла
        String fileUrl = "https://api.telegram.org/file/bot" + studentHelperBot.getBotToken() + "/" + telegramFile.getFilePath();

        // Открываем InputStream для скачивания файла
        return URI.create(fileUrl).toURL().openStream();
    }

    private ByteArrayOutputStream convertWordToPdf(InputStream wordInputStream) throws IOException {
        // Открываем Word-документ
        XWPFDocument document = new XWPFDocument(wordInputStream);

        // Создаем PDF-документ в памяти
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(pdfOutputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        com.itextpdf.layout.Document pdfDoc = new com.itextpdf.layout.Document(pdfDocument);

        // Перебираем параграфы из Word-документа и добавляем их в PDF
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String paragraphText = paragraph.getText();
            // Добавляем параграф в PDF-документ
            pdfDoc.add(new Paragraph(paragraphText));
        }

        // Закрываем PDF-документ
        pdfDoc.close();

        return pdfOutputStream;
    }

    private void sendPdfDocument(String chatId, ByteArrayOutputStream pdfOutputStream, String originalFileName, Update update) throws TelegramApiException {
        InputStream pdfInputStream = new ByteArrayInputStream(pdfOutputStream.toByteArray());

        // Задаем имя для PDF-документа
        String pdfFileName = originalFileName.replaceAll("\\.[^.]+$", "") + ".pdf";

        // Отправляем PDF-документ обратно пользователю
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(new InputFile(pdfInputStream, pdfFileName));

        studentHelperBot.execute(sendDocument);

        setUserStates(update, States.ACTIVE);
    }

    private void saveProcess(Update update) throws TelegramApiException {
        Document document = update.getMessage().getDocument();
        File execute = studentHelperBot.execute(new GetFile(document.getFileId()));
        java.io.File downloadFile = studentHelperBot.downloadFile(execute);
        fileMetadataDao.insert(update, informationStorage.getDirectory(update.getMessage().getFrom().getId()), downloadFile);
        setView(messageUtils.generateSendMessageWithText(update,
                "Файл успешно сохранён"));
        setUserStates(update, States.ACTIVE);
    }
}
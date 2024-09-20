package com.example.controller.type;

import com.example.controller.StudentHelperBot;
import com.example.controller.UpdateController;
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
        String message = update.getMessage().getText();
        switch (message) {
            case START -> setStartView(update);
            case HELP -> setHelpView(update);
            case UPLOAD_FILE -> setUploadFileView(update);
            default -> log.info(message);
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
                Привет, %s! Я — телеграм-бот, созданный командой талантливых людей.
                Чтобы узнать, какие функции уже доступны, введите команду /help.""",
                message.getChat().getFirstName());
        setView(messageUtils.generateSendMessageWithText(update, text));
    }

    private void setHelpView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
                """
                        Я — бот, который с радостью сохранит ваши готовые работы на сервере, чтобы вы могли получить их в любой момент!
                        
                        Кроме того, вы можете общаться со мной, как с ChatGPT.
                        
                        ⚙️ Команды
                        
                        /start — описание и перезапуск бота
                        /upload_file — загрузка файла на сервер
                        /help — справка""");
        setView(sendMessage);
    }

    private void setUploadFileView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
                "Загрузите файл, который хотите сохранить");
        setView(sendMessage);
    }
}
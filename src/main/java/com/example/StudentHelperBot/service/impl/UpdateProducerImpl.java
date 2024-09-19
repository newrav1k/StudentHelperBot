package com.example.StudentHelperBot.service.impl;

import com.example.StudentHelperBot.service.UpdateProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class UpdateProducerImpl implements UpdateProducer {
    private static final Logger log = LoggerFactory.getLogger(UpdateProducerImpl.class);

    public static final String TEXT_MESSAGE_UPDATE = "text_message_update";
    public static final String DOC_MESSAGE_UPDATE = "doc_message_update";
    public static final String PHOTO_MESSAGE_UPDATE = "photo_message_update";
    public static final String ANSWER_MESSAGE = "answer_message";

    @Override
    public void produce(String message, Update update) {
        log.info(message);
    }
}
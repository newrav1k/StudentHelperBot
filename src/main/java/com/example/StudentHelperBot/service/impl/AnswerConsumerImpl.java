package com.example.StudentHelperBot.service.impl;

import com.example.StudentHelperBot.service.AnswerConsumer;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class AnswerConsumerImpl implements AnswerConsumer {

    @Override
    public void consume(SendMessage sendMessage) {

    }

}
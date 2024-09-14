package com.example.StudentHelperBot.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProducer {
    void produce(String message, Update update);
}
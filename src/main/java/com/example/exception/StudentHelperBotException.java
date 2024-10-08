package com.example.exception;

import java.io.Serial;

public class StudentHelperBotException extends Exception {

    @Serial
    private static final long serialVersionUID = 9200853606774387616L;

    public StudentHelperBotException(String message, Throwable cause) {
        super(message, cause);
    }

    public StudentHelperBotException(String message) {
        super(message);
    }
}
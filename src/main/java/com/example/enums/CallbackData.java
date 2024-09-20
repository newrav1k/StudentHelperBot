package com.example.enums;

import java.util.HashMap;

public enum CallbackData {
    CALLBACK_DATA_SAVE ("callback_data_save"),
    CALLBACK_DATA_CONVERT ("callback_data_convert"),
    CALLBACK_DATA_DELETE ("callback_data_delete"),
    CALLBACK_DATA_CANCEL ("callback_data_cancel");

    private final String title;

    private static final HashMap<String, CallbackData> MAP = new HashMap<>();

    static {
        for (CallbackData value : values()) {
            MAP.put(value.title, value);
        }
    }

    CallbackData(String title) {
        this.title = title;
    }

    public static CallbackData fromString(String title) {
        return MAP.getOrDefault(title.toLowerCase(), null);
    }
}
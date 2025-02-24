package com.example.enums;

import java.util.HashMap;

public enum CallbackData {
    CALLBACK_DATA_SAVE("callback_data_save"),
    CALLBACK_DATA_CONVERT("callback_data_convert"),
    CALLBACK_DATA_CANCEL("callback_data_cancel"),
    CALLBACK_DATA_CANCEL_FILE("callback_data_cancel_file"),
    CALLBACK_DATA_DELETE_DIRECTORY("callback_data_delete_directory"),
    CALLBACK_DATA_ADD_DIRECTORY("callback_data_add_directory"),
    CALLBACK_DATA_CHOOSE_DIRECTORY("callback_data_choose_directory"),
    CALLBACK_DATA_CHANGE_DIRECTORY_NAME("callback_data_change_directory_name"),
    CALLBACK_DATA_ADD_FILE("callback_data_add_file"),
    CALLBACK_DATA_DOWNLOAD_FILE("callback_data_download_file"),
    CALLBACK_DATA_DELETE_FILE("callback_data_delete_file"),
    CALLBACK_DATA_CHANGE_FILE_DIRECTORY("callback_data_change_file_directory"),
    CALLBACK_DATA_CHANGE_FILE_NAME("callback_data_change_file_name"),
    CALLBACK_DATA_CONFIRMATION_YES_DIRECTORY("callback_data_confirmation_yes_directory"),
    CALLBACK_DATA_CONFIRMATION_NO_DIRECTORY("callback_data_confirmation_no_directory"),
    CALLBACK_DATA_CONFIRMATION_YES_FILE("callback_data_confirmation_yes_file"),
    CALLBACK_DATA_CONFIRMATION_NO_FILE("callback_data_confirmation_no_file");

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
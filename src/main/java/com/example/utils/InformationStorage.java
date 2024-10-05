package com.example.utils;

import com.example.entity.Directory;
import com.example.enums.States;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
@Component
public class InformationStorage {
    private Map<Long, States> userStates = new HashMap<>();

    private Map<Long, File> previousFiles = new HashMap<>();

    private Map<Long, Directory> previousDirectories = new HashMap<>();

    private Map<Long, Document> previousDocument = new HashMap<>();

    public States getState(long id) {
        return userStates.getOrDefault(id, States.ACTIVE);
    }

    public File getFile(long id) {
        return previousFiles.get(id);
    }

    public Directory getDirectory(long id) {
        return previousDirectories.get(id);
    }

    public Document getDocument(long id) {
        return previousDocument.get(id);
    }

    public void putState(long id, States state) {
        userStates.put(id, state);
        log.info("Пользователю {} установлено новое состояние - {}", id, state);
    }

    public void putFile(long id, File file) {
        previousFiles.put(id, file);
        log.info("У пользователя {} обновлён последний файл - {}", id, file);
    }

    public void putDirectory(long id, Directory directory) {
        previousDirectories.put(id, directory);
        log.info("У пользователя {} обновлёна выбранная директория - {}", id, directory);
    }

    public void putDocument(long id, Document document) {
        previousDocument.put(id, document);
    }

    public void clearData(long id) {
        userStates.remove(id);
        previousFiles.remove(id);
        previousDirectories.remove(id);
    }
}
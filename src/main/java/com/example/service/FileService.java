package com.example.service;

import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import com.example.exception.StudentHelperBotException;
import com.example.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.List;

@Service
public class FileService {

    private final FileRepository fileRepository;

    @Autowired
    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Transactional
    public void save(Update update, Directory directory, File file, Document document) throws StudentHelperBotException {
        try (FileChannel channel = FileChannel.open(Path.of(file.toURI()))) {
            long size = channel.size();

            ByteBuffer buffer = ByteBuffer.allocate((int) size);
            int read = channel.read(buffer);

            if (read == size) {
                buffer.flip();

                byte[] bytes = new byte[(int) size];
                buffer.get(bytes);

                String fileName;
                if (update.hasCallbackQuery()) {
                    fileName = document.getFileName();
                } else {
                    fileName = update.getMessage().getCaption() == null ? document.getFileName() :
                            update.getMessage().getCaption() + "."
                            + document.getFileName().split("\\.")[1];
                }

                FileMetadata fileMetadata = FileMetadata.builder()
                        .directory(directory)
                        .title(fileName)
                        .content(bytes)
                        .build();

                fileRepository.save(fileMetadata);
            }
        } catch (IOException exception) {
            throw new StudentHelperBotException("Не удалось сохранить файл", exception);
        }
    }

    @Transactional
    public void deleteById(long id) {
        fileRepository.deleteById(id);
    }

    @Transactional
    public void deleteByTitle(Directory directory, String title) {
        fileRepository.deleteByDirectoryAndTitle(directory, title);
    }

    @Transactional
    public void rename(FileMetadata fileMetadata, String newName) {
        fileMetadata.setTitle(newName + "." + fileMetadata.getTitle().split("\\.")[1]);
        fileRepository.save(fileMetadata);
    }

    @Transactional
    public void moveToDirectory(Directory directory, FileMetadata fileMetadata) {
        fileRepository.updateByDirectory(directory, fileMetadata);
    }

    @Transactional
    public FileMetadata findBySerial(Directory directory, int serial) {
        return fileRepository.findAllByDirectory(directory).get(serial - 1);
    }

    @Transactional
    public List<FileMetadata> findAll(Directory directory) {
        return fileRepository.findAllByDirectory(directory);
    }
}
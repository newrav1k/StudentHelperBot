package com.example.service;

import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import com.example.entity.Student;
import com.example.exception.StudentHelperBotException;
import com.example.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final DirectoryService directoryService;
    private final StudentService studentService;

    @Autowired
    public FileService(FileRepository fileRepository, DirectoryService directoryService, StudentService studentService) {
        this.fileRepository = fileRepository;
        this.directoryService = directoryService;
        this.studentService = studentService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Cacheable(value = "files.title", key = "#document.fileName")
    public FileMetadata save(Update update, Directory directory, File file, Document document) throws StudentHelperBotException {
        FileMetadata fileMetadata = null;
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

                Student student = studentService.findById(update.getMessage().getFrom().getId());
                fileMetadata = FileMetadata.builder()
                        .directory(Optional.ofNullable(directory).orElse(directoryService.save(student, "Прочее")))
                        .title(fileName)
                        .content(bytes)
                        .build();

                fileRepository.save(fileMetadata);
            }
        } catch (IOException exception) {
            throw new StudentHelperBotException("Не удалось сохранить файл", exception);
        }
        return fileMetadata;
    }

    @Transactional
    @CacheEvict(value = "files.id", key = "#id")
    public void deleteById(long id) {
        fileRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = "files.title", key = "#title")
    public void deleteByTitle(Directory directory, String title) {
        fileRepository.deleteByDirectoryAndTitle(directory, title);
    }

    @Transactional
    @CachePut(value = "files.title", key = "#newName")
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
package com.example.dao;

import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public interface FileMetadataDao {
    void insert(Update update, File file);

    File findById(Update update, Directory directory, String number);

    List<File> findAll(Update update, Directory directory);

    @SneakyThrows
    default File convertToFile(FileMetadata fileMetadata) {
        Path path = Paths.get(fileMetadata.getTitle());
        try (FileChannel channel = FileChannel.open(path)) {
            long size = channel.size();

            ByteBuffer buffer = ByteBuffer.allocate((int) size);
            buffer.put(fileMetadata.getContent());

            buffer.flip();
            byte[] bytes = new byte[(int) size];
            buffer.get(bytes);

            Files.write(path, buffer.array());
        }
        return path.toFile();
    }
}
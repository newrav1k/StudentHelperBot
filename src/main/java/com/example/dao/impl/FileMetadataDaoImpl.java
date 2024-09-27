package com.example.dao.impl;

import com.example.dao.FileMetadataDao;
import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import com.example.entity.Student;
import com.example.utils.HibernateUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class FileMetadataDaoImpl implements FileMetadataDao {

    @Override
    public void insert(Update update, File file) {
        User user = update.getMessage().getFrom();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            Student student = session.get(Student.class, user.getId());

            Document document = update.getMessage().getDocument();

            try (FileChannel channel = FileChannel.open(Path.of(file.toURI()))) {
                long size = channel.size();

                ByteBuffer buffer = ByteBuffer.allocate((int) size);
                int read = channel.read(buffer);

                if (read == size) {
                    buffer.flip();

                    byte[] bytes = new byte[(int) size];
                    buffer.get(bytes);

                    Directory directory = session.createQuery("from Directory where student.id = :id and title = :title", Directory.class)
                            .setParameter("id", user.getId())
                            .setParameter("title", "Прочее")
                            .getSingleResult();
                    student.addDirectory(directory);

                    FileMetadata fileMetadata = FileMetadata.builder()
                            .title(
                                    update.getMessage().getCaption() == null ? document.getFileName() :
                                            update.getMessage().getCaption() + "."
                                                    + document.getFileName().split("\\.")[1]
                            )
                            .content(bytes)
                            .build();
                    directory.addFileMetadata(fileMetadata);
                }
            }

            session.getTransaction().commit();
        } catch (IOException exception) {
            log.error(exception.getMessage());
        }
    }

    @Override
    public File findById(Update update, Directory directory, String number) {
        File file;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            FileMetadata fileMetadata = session.createQuery("from FileMetadata where directory.id = :id", FileMetadata.class)
                    .setParameter("id", directory.getId())
                    .getResultList().get(Integer.parseInt(number) - 1);

            file = convertToFile(fileMetadata);

            session.getTransaction().commit();
        }
        return file;
    }

    @Override
    public List<File> findAll(Update update, Directory directory) {
        List<File> files;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            files = session.createQuery("from FileMetadata where directory.id = :id",
                            FileMetadata.class).setParameter("id", directory.getId())
                    .stream().map(this::convertToFile).toList();

            session.getTransaction().commit();
        }
        return files;
    }
}
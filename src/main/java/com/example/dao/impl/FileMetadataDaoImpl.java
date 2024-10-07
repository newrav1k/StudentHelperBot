package com.example.dao.impl;

import com.example.dao.FileMetadataDao;
import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import com.example.entity.Student;
import com.example.utils.HibernateUtil;
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
import java.util.Optional;

@Slf4j
public class FileMetadataDaoImpl implements FileMetadataDao {

    @Override
    public void insert(Update update, Directory directory, File file) {
        User user = receivedUser(update);
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

                    Directory dir = session.createQuery("from Directory where student.id = :id and title = :title", Directory.class)
                            .setParameter("id", user.getId())
                            .setParameter("title", Optional.ofNullable(directory.getTitle()).orElse("Прочее"))
                            .getSingleResult();
                    student.addDirectory(dir);

                    FileMetadata fileMetadata = FileMetadata.builder()
                            .title(
                                    update.getMessage().getCaption() == null ? document.getFileName() :
                                            update.getMessage().getCaption() + "."
                                                    + document.getFileName().split("\\.")[1]
                            )
                            .content(bytes)
                            .build();
                    dir.addFileMetadata(fileMetadata);
                }
            }

            session.getTransaction().commit();
        } catch (IOException exception) {
            log.error(exception.getMessage());
        }
    }

    @Override
    public void deleteBySerial(Student student, Directory directory, int serial) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.get(Directory.class, directory.getId()).getFilesMetadata().remove(serial - 1);

            session.getTransaction().commit();
        }
    }

    @Override
    public void moveToDirectory(Student student, Directory directory, FileMetadata fileMetadata) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.createMutationQuery("update FileMetadata set directory = :directory where id = :id")
                    .setParameter("directory", directory).setParameter("id", fileMetadata.getId()).executeUpdate();

            session.getTransaction().commit();
        }
    }

    @Override
    public FileMetadata findBySerial(Student student, Directory directory, int serial) {
        FileMetadata fileMetadata;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            fileMetadata = session.createQuery("from FileMetadata where directory.id = :id", FileMetadata.class)
                    .setParameter("id", directory.getId())
                    .getResultList().get(serial - 1);

            session.getTransaction().commit();
        }
        return fileMetadata;
    }

    @Override
    public List<FileMetadata> findAll(Student student, Directory directory) {
        List<FileMetadata> files;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            files = session.createQuery("from FileMetadata where directory.id = :id",
                            FileMetadata.class).setParameter("id", directory.getId())
                    .stream().toList();

            session.getTransaction().commit();
        }
        return files;
    }

    private static User receivedUser(Update update) {
        return update.hasCallbackQuery() ? update.getCallbackQuery().getFrom() : update.getMessage().getFrom();
    }
}
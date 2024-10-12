package com.example.dao.impl;

import com.example.dao.FileMetadataDao;
import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import com.example.entity.Student;
import com.example.exception.StudentHelperBotException;
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

@Slf4j
public class FileMetadataDaoImpl implements FileMetadataDao {

    private static final String FILE_NOT_FOUND = "Такой файл не найден";

    @Override
    public void insert(Update update, Directory directory, File file, Document document) throws StudentHelperBotException {
        User user = receivedUser(update);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            Student student = session.get(Student.class, user.getId());

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
                            .setParameter("title", directory == null ? "Прочее" : directory.getTitle())
                            .getSingleResult();
                    student.addDirectory(dir);

                    String fileName;
                    if (update.hasCallbackQuery()) {
                        fileName = document.getFileName();
                    } else {
                        fileName = update.getMessage().getCaption() == null ? document.getFileName() :
                                update.getMessage().getCaption() + "."
                                        + document.getFileName().split("\\.")[1];
                    }

                    FileMetadata fileMetadata = FileMetadata.builder()
                            .title(fileName)
                            .content(bytes)
                            .build();
                    dir.addFileMetadata(fileMetadata);
                }
            }

            session.getTransaction().commit();
        } catch (IOException exception) {
            log.error(exception.getMessage());
        } catch (Exception exception) {
            throw new StudentHelperBotException("Не удалось сохранить файл", exception);
        }
    }

    @Override
    public void deleteBySerial(Student student, Directory directory, int serial) throws StudentHelperBotException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.get(Directory.class, directory.getId()).getFilesMetadata().remove(serial - 1);

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException(FILE_NOT_FOUND, exception);
        }
    }

    @Override
    public void deleteByTitle(Student student, Directory directory, String title) {
// Добавить реализацию удаления файла через title
    }

    @Override
    public void changeFileName(Student student, FileMetadata fileMetadata, String newFileName) throws StudentHelperBotException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.createMutationQuery("update FileMetadata set title = :title where id = :id")
                    .setParameter("title", newFileName + fileMetadata.getTitle().substring(fileMetadata.getTitle().lastIndexOf(".")))
                    .setParameter("id", fileMetadata.getId()).executeUpdate();

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException("Не удалось переименовать файл", exception);
        }
    }

    @Override
    public void moveToDirectory(Student student, Directory directory, FileMetadata fileMetadata) throws StudentHelperBotException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.createMutationQuery("update FileMetadata set directory = :directory where id = :id")
                    .setParameter("directory", directory).setParameter("id", fileMetadata.getId()).executeUpdate();

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException("Не удалось переместить файл в другую директорию", exception);
        }
    }

    ///java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
    /// 	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:100) ~[na:na]
    /// 	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:106) ~[na:na]
    /// 	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:302) ~[na:na]
    /// 	at java.base/java.util.Objects.checkIndex(Objects.java:365) ~[na:na]
    /// 	at java.base/java.util.ArrayList.get(ArrayList.java:428) ~[na:na]
    /// 	at com.example.dao.impl.FileMetadataDaoImpl.findBySerial(FileMetadataDaoImpl.java:117) ~[classes/:na]
    @Override
    public FileMetadata findBySerial(Student student, Directory directory, int serial) throws StudentHelperBotException {
        FileMetadata fileMetadata;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            ///java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
            /// 	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:100) ~[na:na]
            /// 	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:106) ~[na:na]
            /// 	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:302) ~[na:na]
            /// 	at java.base/java.util.Objects.checkIndex(Objects.java:365) ~[na:na]
            /// 	at java.base/java.util.ArrayList.get(ArrayList.java:428) ~[na:na]
            /// 	at com.example.dao.impl.FileMetadataDaoImpl.findBySerial(FileMetadataDaoImpl.java:117) ~[classes/:na] ///
            fileMetadata = session.createQuery("from FileMetadata where directory.id = :id", FileMetadata.class)
                    .setParameter("id", directory.getId())
                    .getResultList().get(serial - 1);

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException(FILE_NOT_FOUND, exception);
        }
        return fileMetadata;
    }

    @Override
    public List<FileMetadata> findAll(Student student, Directory directory) throws StudentHelperBotException {
        List<FileMetadata> files;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            files = session.createQuery("from FileMetadata where directory.id = :id",
                            FileMetadata.class).setParameter("id", directory.getId())
                    .stream().toList();

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException("Не удалось отобразить список не файлов", exception);
        }
        return files;
    }

    private static User receivedUser(Update update) {
        return update.hasCallbackQuery() ? update.getCallbackQuery().getFrom() : update.getMessage().getFrom();
    }
}
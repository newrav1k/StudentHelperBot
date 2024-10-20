package com.example.dao.impl;

import com.example.dao.FileMetadataDao;
import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import com.example.entity.Student;
import com.example.exception.StudentHelperBotException;
import com.example.utils.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
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

    private static final String FILE_NOT_FOUND = "Такой файл не найден";

    @Override
    public synchronized void insert(Update update, Directory directory, File file, Document document) throws StudentHelperBotException {
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
                            .setParameter("title", Optional.ofNullable(directory)
                                    .map(Directory::getTitle).orElse("Прочее"))
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
        } catch (ConstraintViolationException exception) {
            Document doc = new Document();
            String[] split = document.getFileName().split("\\.");
            String fileName = file.getName().split("\\.")[0];
            doc.setFileName(fileName.length() < 32 ? fileName : fileName.substring(0, 32) + "." + split[split.length - 1]);
            insert(update, directory, file, doc);
            throw new StudentHelperBotException("К сожалению, файл с таким именем уже существует, " +
                                                "поэтому я сохранил ваш файл под другим именем: " +
                                                doc.getFileName(), exception);
        } catch (Exception exception) {
            throw new StudentHelperBotException("Не удалось сохранить файл", exception);
        }
    }

    @Override
    public synchronized void deleteBySerial(Student student, Directory directory, int serial) throws StudentHelperBotException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.get(Directory.class, directory.getId()).getFilesMetadata().remove(serial - 1);

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException(FILE_NOT_FOUND, exception);
        }
    }

    @Override
    public synchronized void deleteByTitle(Student student, Directory directory, String title) throws StudentHelperBotException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.createMutationQuery("delete from FileMetadata where directory.id = :id and title = :title")
                    .setParameter("id", directory.getId())
                    .setParameter("title", title).executeUpdate();

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException(FILE_NOT_FOUND, exception);
        }
    }

    @Override
    public synchronized void renameFile(Student student, FileMetadata fileMetadata, String newFileName) throws StudentHelperBotException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.createMutationQuery("update FileMetadata set title = :title where id = :id")
                    .setParameter("title", newFileName + fileMetadata.getTitle().substring(fileMetadata.getTitle().lastIndexOf(".")))
                    .setParameter("id", fileMetadata.getId())
                    .executeUpdate();

            session.getTransaction().commit();
        } catch (ConstraintViolationException exception) {
            throw new StudentHelperBotException("Такой файл уже существует", exception);
        } catch (Exception exception) {
            throw new StudentHelperBotException("Не удалось переименовать файл", exception);
        }
    }

    @Override
    public synchronized void moveToDirectory(Student student, Directory directory, FileMetadata fileMetadata) throws StudentHelperBotException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.createMutationQuery("update FileMetadata set directory = :directory where id = :id")
                    .setParameter("directory", directory).setParameter("id", fileMetadata.getId()).executeUpdate();

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException("Не удалось переместить файл в другую директорию", exception);
        }
    }

    @Override
    public synchronized FileMetadata findBySerial(Student student, Directory directory, int serial) throws StudentHelperBotException {
        FileMetadata fileMetadata;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

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
    public synchronized List<FileMetadata> findAll(Student student, Directory directory) throws StudentHelperBotException {
        List<FileMetadata> files;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            files = session.createQuery("from FileMetadata where directory.id = :id",
                            FileMetadata.class).setParameter("id", directory.getId())
                    .stream().toList();

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException("Не удалось отобразить список файлов", exception);
        }
        return files;
    }

    private static User receivedUser(Update update) {
        return update.hasCallbackQuery() ? update.getCallbackQuery().getFrom() : update.getMessage().getFrom();
    }
}
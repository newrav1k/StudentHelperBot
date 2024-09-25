package com.example.database;

import com.example.entity.FileMetadata;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
@NoArgsConstructor
public class HibernateRunner {

    public void saveFile(File file, String fileName) {
        Configuration configuration = new Configuration().configure();
        configuration.configure();
        try (SessionFactory sessionFactory = configuration.buildSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            FileMetadata fileMetadata = FileMetadata.builder()
                    .name(fileName)
                    .build();

            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                fileMetadata.setContent(bytes);
                fileMetadata.setSize(bytes.length);
            } catch (IOException exception) {
                log.error(exception.getMessage());
            }
            session.save(fileMetadata);

            session.getTransaction().commit();
        }
    }

    public File getFile(String id) {
        Configuration configuration = new Configuration().configure();
        configuration.configure();

        FileMetadata fileMetadata = getFileMetadata().get(Integer.parseInt(id) - 1);

        return getFile(fileMetadata);
    }

    public List<FileMetadata> getFileMetadata() {
        Configuration configuration = new Configuration().configure();
        configuration.configure();
        List<FileMetadata> fileMetadata;
        try (SessionFactory sessionFactory = configuration.buildSessionFactory();
             Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            fileMetadata = new LinkedList<>(session.createQuery("from FileMetadata", FileMetadata.class).getResultList());

            session.getTransaction().commit();
        }
        return fileMetadata;
    }

    private static File getFile(FileMetadata fileMetadata) {
        Path path = Paths.get(fileMetadata.getName());
        File file = null;

        try {
            Path filePath = Files.write(path, fileMetadata.getContent());
            file = filePath.toFile();
        } catch (IOException exception) {
            log.error(exception.getMessage());
        }

        return file;
    }
}
package com.example.dao.impl;

import com.example.dao.DirectoryDao;
import com.example.entity.Directory;
import com.example.entity.Student;
import com.example.utils.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DirectoryDaoImpl implements DirectoryDao {

    @Override
    public void insert(Update update, String title) {
        User user = update.getMessage().getFrom();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            Student student = session.get(Student.class, user.getId());
            Directory directory = Directory.builder()
                    .title(title)
                    .build();

            if (student.getDirectories().contains(directory)) {
                return;
            }
            student.addDirectory(directory);

            session.getTransaction().commit();
        }
    }

    @Override
    public void update(Update update, String title, String newTitle) {
        User user = update.getMessage().getFrom();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            Directory directory = session.createQuery("from Directory where title = :title and student.id = :id", Directory.class)
                    .setParameter("title", title)
                    .setParameter("id", user.getId())
                    .getSingleResult();
            directory.setTitle(newTitle);

            session.getTransaction().commit();
        }
    }

    @Override
    public void delete(Update update, String title) {
        User user = update.getMessage().getFrom();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            Directory directory = session.createQuery("delete from Directory where student.id = :id and title = :title", Directory.class)
                    .setParameter("id", user.getId())
                    .setParameter("title", title)
                    .getSingleResult();
            session.detach(directory);

            session.getTransaction().commit();
        }
    }

    @Override
    public Directory findByTitle(Update update, String title) {
        User user = update.getMessage().getFrom();
        Directory directory;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            directory = session.createQuery("from Directory where student.id = :id and title = :title", Directory.class)
                    .setParameter("id", user.getId())
                    .setParameter("title", title)
                    .getSingleResult();

            session.getTransaction().commit();
        }
        return directory;
    }

    @Override
    public List<Directory> findAll(Update update) {
        List<Directory> directories;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            directories = new ArrayList<>(session.createQuery("from Directory where student.id = :id", Directory.class)
                    .setParameter("id", update.getMessage().getFrom().getId()).list());
            session.getTransaction().commit();
        }
        return directories;
    }
}
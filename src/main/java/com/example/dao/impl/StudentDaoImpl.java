package com.example.dao.impl;

import com.example.dao.StudentDao;
import com.example.entity.Directory;
import com.example.entity.PersonalInfo;
import com.example.entity.Student;
import com.example.exception.StudentHelperBotException;
import com.example.utils.HibernateUtil;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
public class StudentDaoImpl implements StudentDao {

    @Override
    public synchronized void insert(Update update) {
        User user = update.getMessage().getFrom();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            Student student = Student.builder()
                    .id(user.getId())
                    .personalInfo(PersonalInfo.builder()
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .build())
                    .build();

            Directory directory;
            try {
                directory = session.createQuery("from Directory where student.id = :id and title = :title", Directory.class)
                        .setParameter("id", student.getId())
                        .setParameter("title", "Прочее")
                        .getSingleResult();
            } catch (NoResultException exception) {
                directory = Directory.builder().title("Прочее").build();
            }
            session.persist(directory);
            directory.setStudent(student);
            session.persist(student);

            session.getTransaction().commit();
        }
    }

    @Override
    public synchronized Student findById(Update update) throws StudentHelperBotException {
        Student student = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            student = session.createQuery("from Student where id = :id", Student.class)
                    .setParameter("id", update.hasCallbackQuery() ?
                            update.getCallbackQuery().getFrom().getId() : update.getMessage().getFrom().getId())
                    .getSingleResult();

            session.getTransaction().commit();
        } catch (NoResultException exception) {
            insert(update);
        } catch (RuntimeException exception) {
            throw new StudentHelperBotException("Странно, не могу получить информацию о Вас...");
        }
        return student;
    }
}
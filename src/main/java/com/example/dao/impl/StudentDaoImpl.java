package com.example.dao.impl;

import com.example.dao.StudentDao;
import com.example.entity.Directory;
import com.example.entity.PersonalInfo;
import com.example.entity.Student;
import com.example.utils.HibernateUtil;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
public class StudentDaoImpl implements StudentDao {

    @Override
    public void insert(Update update) {
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
            session.saveOrUpdate(directory);
            directory.setStudent(student);
            session.saveOrUpdate(student);

            session.getTransaction().commit();
        }
    }

    @Override
    public Student findById(Long id) {
        Student student;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            student = session.createQuery("from Student where id = :id", Student.class)
                    .setParameter("id", id)
                    .getSingleResult();

            session.getTransaction().commit();
        }
        return student;
    }
}
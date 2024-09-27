package com.example.dao.impl;

import com.example.dao.StudentDao;
import com.example.entity.Directory;
import com.example.entity.PersonalInfo;
import com.example.entity.Student;
import com.example.utils.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.tool.schema.spi.SqlScriptException;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;

@Slf4j
public class StudentDaoImpl implements StudentDao {

    private static final Map<Long, Student> users = new HashMap<>();

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

            Directory directory = Directory.builder()
                    .id(1L)
                    .title("Прочее").build();
            session.save(directory);
            directory.setStudent(student);
            session.saveOrUpdate(student);

            users.put(user.getId(), student);

            session.getTransaction().commit();
        }
    }

    @Override
    public void update(Update update) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();


            session.getTransaction().commit();
        }
    }

    @Override
    public void delete(Update update) {
        Long chatId = update.getMessage().getChatId();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            Student student = session.get(Student.class, chatId);
            session.delete(student);

            session.getTransaction().commit();
        }
    }

    @Override
    public Student findById(Long id) {
        return users.get(id);
    }

    @Override
    public List<Student> findAll() {
        Set<Map.Entry<Long, Student>> entries = users.entrySet();
        List<Student> students = new ArrayList<>();
        for (Map.Entry<Long, Student> entry : entries) {
            students.add(entry.getValue());
        }
        return students;
    }
}
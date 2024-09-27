package com.example;

import com.example.entity.Directory;
import com.example.entity.Student;
import com.example.utils.HibernateUtil;
import lombok.Cleanup;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

class HibernateConfigurationTest {

    private static final Long STUDENT_ID = 1105292384L;

    @Test
    void deleteStudent() {
        @Cleanup SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();

        Directory directory = session.get(Directory.class, 1L);
        session.delete(directory);

        session.getTransaction().commit();
    }

    @Test
    void insertDirectory() {
        @Cleanup SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();

        Student student = session.get(Student.class, STUDENT_ID);

        Directory directory = Directory.builder()
                .title("Java").build();
        student.addDirectory(directory);

        session.getTransaction().commit();
    }

    @Test
    void insertStudent() {
        @Cleanup SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();

        Student student = Student.builder()
                .id(STUDENT_ID).build();
        session.save(student);

        session.getTransaction().commit();
    }
}
package com.example.dao.impl;

import com.example.dao.DirectoryDao;
import com.example.entity.Directory;
import com.example.entity.Student;
import com.example.exception.StudentHelperBotException;
import com.example.utils.HibernateUtil;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class DirectoryDaoImpl implements DirectoryDao {

    private static final String DIRECTORY_NOT_FOUND = "Такая директория не найдена";

    @Override
    public synchronized void insert(Student student, String title) throws StudentHelperBotException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            Student user = session.get(Student.class, student.getId());

            Directory directory = Optional.ofNullable(findByTitle(student, title)).orElse(Directory.builder().title(title).student(user).build());

            session.saveOrUpdate(directory);

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException("Не удалось создать директорию");
        }
    }

    @Override
    public synchronized void update(Student student, String title) throws StudentHelperBotException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.createQuery("update Directory set title=:title where student.id=:id", Directory.class)
                    .setParameter("title", title)
                    .setParameter("id", student.getId())
                    .executeUpdate();

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException(DIRECTORY_NOT_FOUND, exception);
        }
    }

    @Override
    public synchronized void renameDirectory(Student student, Directory directory, String title) throws StudentHelperBotException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.createMutationQuery("update Directory set title=:title where id=:id and student.id = :student_id")
                    .setParameter("title", title)
                    .setParameter("id", directory.getId())
                    .setParameter("student_id", student.getId())
                    .executeUpdate();

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException(DIRECTORY_NOT_FOUND, exception);
        }
    }

    @Override
    public synchronized void deleteByTitle(Student student, String title) throws StudentHelperBotException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.createMutationQuery("delete from Directory where student.id=:id and title=:title")
                    .setParameter("id", student.getId())
                    .setParameter("title", title)
                    .executeUpdate();

            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException(DIRECTORY_NOT_FOUND, exception);
        }
    }

    @Override
    public synchronized Directory findByTitle(Student student, String title) throws StudentHelperBotException {
        Directory directory;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            directory = session.createQuery("from Directory where student.id=:id and title=:title", Directory.class)
                    .setParameter("id", student.getId())
                    .setParameter("title", title)
                    .getSingleResult();

            session.getTransaction().commit();
        } catch (NoResultException exception) {
            return null;
        } catch (Exception exception) {
            throw new StudentHelperBotException(DIRECTORY_NOT_FOUND, exception);
        }
        return directory;
    }

    @Override
    public synchronized List<Directory> findAll(Student student) throws StudentHelperBotException {
        List<Directory> directories;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            directories = new ArrayList<>(session.createQuery("from Directory where student.id = :id", Directory.class)
                    .setParameter("id", student.getId()).list());
            session.getTransaction().commit();
        } catch (Exception exception) {
            throw new StudentHelperBotException("Не удалось отобразить список директорий");
        }
        return directories;
    }
}
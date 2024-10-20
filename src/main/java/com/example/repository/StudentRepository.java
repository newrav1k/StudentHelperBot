package com.example.repository;

import com.example.entity.Directory;
import com.example.entity.PersonalInfo;
import com.example.entity.Student;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @Transactional
    default Student save(Update update) {
        Student student = null;
        try {
            User user = update.hasCallbackQuery() ? update.getCallbackQuery().getFrom() : update.getMessage().getFrom();
            Directory directory = Directory.builder()
                    .title("Прочее")
                    .build();
            student = Student.builder()
                    .id(user.getId())
                    .personalInfo(PersonalInfo.builder()
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .build())
                    .build();
            directory.setStudent(student);
            student.getDirectories().add(directory);
            saveAndFlush(student);
        } catch (DataIntegrityViolationException | UnexpectedRollbackException ignored) {
            // student || directory exists
        }
        return student;
    }

}
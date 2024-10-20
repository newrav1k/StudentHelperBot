package com.example.repository;

import com.example.entity.Directory;
import com.example.entity.PersonalInfo;
import com.example.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    default void save(Update update) {
        User user = update.hasCallbackQuery() ? update.getCallbackQuery().getFrom() : update.getMessage().getFrom();
        Directory directory = Directory.builder()
                .title("Прочее")
                .build();
        Student student = Student.builder()
                .id(user.getId())
                .personalInfo(PersonalInfo.builder()
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .build())
                .build();
        directory.setStudent(student);
        student.getDirectories().add(directory);
        save(student);
    }

}
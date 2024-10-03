package com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ToString(exclude = "directories")
@Table(name = "students", schema = "student_helper_bot")
public class Student {
    @Id
    private Long id;

    @Embedded
    private PersonalInfo personalInfo;

    @Builder.Default
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Directory> directories = new ArrayList<>();

    public void addDirectory(Directory directory) {
        this.directories.add(directory);
        directory.setStudent(this);
    }
}
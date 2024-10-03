package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ToString(exclude = "directory")
@Table(name = "files", schema = "student_helper_bot")
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content", columnDefinition = "bytea")
    private byte[] content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "directory_id")
    private Directory directory;

    @SneakyThrows
    public static File convertToFile(FileMetadata fileMetadata) {
        Path path = Paths.get(fileMetadata.getTitle());
        File file = path.toFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileMetadata.getContent());
        }
        return file;
    }
}
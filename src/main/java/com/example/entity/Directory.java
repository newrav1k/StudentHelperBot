package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EqualsAndHashCode(of = "title")
@ToString(exclude = "filesMetadata")
@Table(name = "directories", schema = "student_helper_bot")
public class Directory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Builder.Default
    @OneToMany(mappedBy = "directory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FileMetadata> filesMetadata = new ArrayList<>();

    public void addFileMetadata(FileMetadata fileMetadata) {
        filesMetadata.add(fileMetadata);
        fileMetadata.setDirectory(this);
    }
}
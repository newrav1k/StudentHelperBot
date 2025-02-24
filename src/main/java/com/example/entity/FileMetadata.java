package com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ToString(exclude = "directory")
@Table(name = "files", schema = "student_helper_bot")
public class FileMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = -1715216232552386853L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content", columnDefinition = "bytea")
    private byte[] content;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "directory_id")
    private Directory directory;

    public String getExtension() {
        return title.substring(title.lastIndexOf(".") + 1);
    }

    public static InputStream convertToInputStream(FileMetadata fileMetadata) {
        return new ByteArrayInputStream(fileMetadata.getContent());
    }
}
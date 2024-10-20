package com.example.repository;

import com.example.entity.Directory;
import com.example.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileMetadata, Long> {

    List<FileMetadata> findAllByDirectory(Directory directory);

    void deleteByDirectoryAndTitle(Directory directory, String title);

    default void updateByDirectory(Directory directory, FileMetadata fileMetadata) {
        fileMetadata.setDirectory(directory);
        save(fileMetadata);
    }
}
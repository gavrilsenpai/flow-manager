package com.example.flowmanager.repository;

import com.example.flowmanager.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {
}

package com.example.flowmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    private String id;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "source_bucket", nullable = false)
    private String sourceBucket;

    @Column(name = "source_object_key", nullable = false)
    private String sourceObjectKey;

    @Column(name = "target_bucket")
    private String targetBucket;

    @Column(name = "target_object_key")
    private String targetObjectKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FileStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public FileMetadata(String id, String originalFileName, String sourceBucket, String sourceObjectKey) {
        this.id = id;
        this.originalFileName = originalFileName;
        this.sourceBucket = sourceBucket;
        this.sourceObjectKey = sourceObjectKey;
        this.status = FileStatus.IN_PROGRESS;
    }
}

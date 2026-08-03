package com.example.flowmanager.mapper;

import com.example.flowmanager.dto.FileStatusResponse;
import com.example.flowmanager.entity.FileMetadata;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {

    public FileStatusResponse toResponse(FileMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        return new FileStatusResponse(
                metadata.getId(),
                metadata.getOriginalFileName(),
                metadata.getStatus(),
                metadata.getErrorMessage(),
                metadata.getCreatedAt(),
                metadata.getUpdatedAt()
        );
    }
}

package com.example.flowmanager.controller;

import com.example.flowmanager.dto.FileStatusResponse;
import com.example.flowmanager.entity.FileMetadata;
import com.example.flowmanager.service.FileManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileManagementService fileManagementService;

    @PostMapping
    public ResponseEntity<FileStatusResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        FileMetadata metadata = fileManagementService.uploadAndStartConversion(file);
        return ResponseEntity.ok(toResponse(metadata));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<FileStatusResponse> getStatus(@PathVariable String id) {
        FileMetadata metadata = fileManagementService.getFileMetadata(id);
        return ResponseEntity.ok(toResponse(metadata));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String id) {
        FileMetadata metadata = fileManagementService.getFileMetadata(id);
        InputStream inputStream = fileManagementService.downloadConvertedFile(id);

        String pdfFileName = metadata.getOriginalFileName() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfFileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(inputStream));
    }

    private FileStatusResponse toResponse(FileMetadata metadata) {
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

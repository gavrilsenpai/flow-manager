package com.example.flowmanager.mapper;

import com.example.flowmanager.dto.FileStatusResponse;
import com.example.flowmanager.entity.FileMetadata;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {

    FileStatusResponse toResponse(FileMetadata metadata);
}

package com.example.auth_service.dto;

import java.time.LocalDateTime;

public record FileInfoDto(
        String fileName,
        String originalFileName,
        String fileType,
        long size,
        LocalDateTime uploadedAt
) {}
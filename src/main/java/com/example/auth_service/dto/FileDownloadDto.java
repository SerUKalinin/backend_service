package com.example.auth_service.dto;

import java.nio.file.Path;

public record FileDownloadDto(
        Path path,
        String originalFileName,
        String contentType
) {}

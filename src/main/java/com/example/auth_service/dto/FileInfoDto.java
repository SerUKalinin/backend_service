package com.example.auth_service.dto;

import java.time.LocalDateTime;

/**
 * DTO с метаинформацией о загруженном файле.
 *
 * <p>Используется для передачи информации о файле без доступа к его содержимому.
 * Применяется в ответах API и на уровне сервисов.</p>
 *
 * @param fileName имя файла, используемое для хранения в системе
 * @param originalFileName оригинальное имя файла, полученное от пользователя
 * @param fileType тип файла (MIME-type или логический тип)
 * @param size размер файла в байтах
 * @param uploadedAt дата и время загрузки файла
 */
public record FileInfoDto(
        String fileName,
        String originalFileName,
        String fileType,
        long size,
        LocalDateTime uploadedAt
) {}

package com.example.auth_service.dto;

import java.nio.file.Path;

/**
 * DTO для передачи информации, необходимой для скачивания файла.
 *
 * <p>Используется на уровне сервисов и контроллеров для формирования ответа
 * при скачивании файла без раскрытия внутренней структуры хранения.</p>
 *
 * @param path путь к файлу в файловой системе
 * @param originalFileName оригинальное имя файла, которое будет предложено пользователю при скачивании
 * @param contentType MIME-тип содержимого файла
 */
public record FileDownloadDto(
        Path path,
        String originalFileName,
        String contentType
) {}

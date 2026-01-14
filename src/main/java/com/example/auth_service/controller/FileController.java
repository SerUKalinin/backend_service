package com.example.auth_service.controller;

import com.example.auth_service.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST-контроллер для управления файлами, прикреплёнными к задачам.
 *
 * Отвечает за загрузку, получение, скачивание и удаление файлов, связанных с задачами.
 * Все операции делегируются {@link FileStorageService}, который реализует бизнес-логику
 * хранения и обработки файлов.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    /**
     * Сервис для работы с файлами задач.
     */
    private final FileStorageService fileStorageService;

    /**
     * Загружает один или несколько файлов для конкретной задачи.
     *
     * @param taskId ID задачи, к которой прикрепляются файлы; должно быть существующим в системе
     * @param files  Массив файлов для загрузки; каждый файл не должен быть пустым
     * @return {@link ResponseEntity} с списком {@link Map}, содержащих информацию о загруженных файлах
     */
    @PostMapping("/upload")
    public ResponseEntity<List<Map<String, String>>> uploadFiles(
            @RequestParam Long taskId,
            @RequestParam MultipartFile[] files) {
        return fileStorageService.uploadFiles(taskId, files);
    }

    /**
     * Получает список всех файлов, прикреплённых к задаче.
     *
     * @param taskId ID задачи; должен существовать в системе
     * @return {@link ResponseEntity} со списком {@link Map}, содержащих метаданные файлов задачи
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<Map<String, String>>> getTaskFiles(@PathVariable Long taskId) {
        return fileStorageService.getTaskFiles(taskId);
    }

    /**
     * Скачивает файл по имени.
     *
     * @param fileName Имя файла для скачивания; должно существовать в хранилище
     * @return {@link ResponseEntity} с {@link Resource} файла для передачи клиенту
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        return fileStorageService.downloadFile(fileName);
    }

    /**
     * Удаляет файл по имени.
     *
     * @param fileName Имя файла для удаления; должно существовать в хранилище
     * @return {@link ResponseEntity} без содержимого (HTTP 200 OK), если удаление прошло успешно
     */
    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName);
        return ResponseEntity.ok().build();
    }
}

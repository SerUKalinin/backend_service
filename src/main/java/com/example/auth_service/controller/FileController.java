package com.example.auth_service.controller;

import com.example.auth_service.model.TaskAttachment;
import com.example.auth_service.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления файлами, прикреплёнными к задачам.
 * <p>
 * Предоставляет методы для загрузки, получения, скачивания и удаления файлов,
 * связанных с задачами. Все операции делегируются в {@link FileStorageService}.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Загружает один или несколько файлов для конкретной задачи.
     *
     * @param taskId ID задачи, к которой прикрепляются файлы
     * @param files  Массив файлов для загрузки
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
     * @param taskId ID задачи
     * @return {@link ResponseEntity} со списком {@link Map}, содержащих информацию о файлах задачи
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<Map<String, String>>> getTaskFiles(@PathVariable Long taskId) {
        return fileStorageService.getTaskFiles(taskId);
    }

    /**
     * Скачивает файл по имени.
     *
     * @param fileName Имя файла для скачивания
     * @return {@link ResponseEntity} с {@link Resource} файла
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        return fileStorageService.downloadFile(fileName);
    }

    /**
     * Удаляет файл по имени.
     *
     * @param fileName Имя файла для удаления
     * @return {@link ResponseEntity} без содержимого (HTTP 200 OK), если удаление прошло успешно
     */
    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName);
        return ResponseEntity.ok().build();
    }
}

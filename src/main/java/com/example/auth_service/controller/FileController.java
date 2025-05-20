package com.example.auth_service.controller;

import com.example.auth_service.exception.FileNotFoundException;
import com.example.auth_service.exception.FileStorageException;
import com.example.auth_service.exception.InvalidFileException;
import com.example.auth_service.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления файлами.
 * Предоставляет эндпоинты для загрузки, скачивания и удаления файлов.
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    @Autowired
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Загружает файлы для конкретной задачи.
     *
     * @param taskId идентификатор задачи
     * @param files массив файлов для загрузки
     * @return ResponseEntity со списком информации о загруженных файлах
     */
    @PostMapping("/upload")
    public ResponseEntity<List<Map<String, String>>> uploadFiles(
            @RequestParam("taskId") Long taskId,
            @RequestParam("files") MultipartFile[] files) {
        log.info("Получен запрос на загрузку файлов для задачи {}", taskId);
        return fileStorageService.uploadFiles(taskId, files);
    }

    /**
     * Получает список файлов для конкретной задачи.
     *
     * @param taskId идентификатор задачи
     * @return ResponseEntity со списком информации о файлах задачи
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<Map<String, String>>> getTaskFiles(@PathVariable Long taskId) {
        log.info("Получен запрос на получение списка файлов для задачи {}", taskId);
        return fileStorageService.getTaskFiles(taskId);
    }

    /**
     * Скачивает файл по его имени.
     * Файл будет отправлен как вложение (attachment).
     *
     * @param fileName имя файла для скачивания
     * @return ResponseEntity с файлом для скачивания
     * @throws InvalidFileException если имя файла пустое
     * @throws FileNotFoundException если файл не найден
     */
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        log.info("Получен запрос на скачивание файла: {}", fileName);
        return fileStorageService.downloadFile(fileName);
    }

    /**
     * Удаляет файл по его имени.
     *
     * @param fileName имя файла для удаления
     * @return ResponseEntity с пустым телом и статусом OK
     * @throws InvalidFileException если имя файла пустое
     * @throws FileNotFoundException если файл не найден
     * @throws FileStorageException если произошла ошибка при удалении файла
     */
    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName) {
        log.info("Получен запрос на удаление файла: {}", fileName);
        return fileStorageService.deleteFile(fileName);
    }
} 
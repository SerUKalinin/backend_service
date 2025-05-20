package com.example.auth_service.service;

import com.example.auth_service.config.FileStorageConfig;
import com.example.auth_service.exception.FileNotFoundException;
import com.example.auth_service.exception.FileStorageException;
import com.example.auth_service.exception.InvalidFileException;
import com.example.auth_service.exception.TaskNotFoundException;
import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskAttachment;
import com.example.auth_service.repository.TaskAttachmentRepository;
import com.example.auth_service.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Сервис для управления файлами в системе.
 * Предоставляет функциональность для загрузки, скачивания и удаления файлов.
 * Включает валидацию файлов и обработку ошибок.
 */
@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final TaskRepository taskRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;

    /** Максимальный размер файла (10MB) */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    /** Список разрешенных типов файлов */
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/jpeg",
            "image/png",
            "image/gif"
    );

    /**
     * Создает новый экземпляр сервиса для работы с файлами.
     * Инициализирует директорию для хранения файлов.
     *
     * @param fileStorageConfig конфигурация хранилища файлов
     * @param taskRepository репозиторий задач
     * @param taskAttachmentRepository репозиторий вложений задач
     * @throws FileStorageException если не удалось создать директорию для хранения файлов
     */
    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig,
                            TaskRepository taskRepository,
                            TaskAttachmentRepository taskAttachmentRepository) {
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath().normalize();
        this.taskRepository = taskRepository;
        this.taskAttachmentRepository = taskAttachmentRepository;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Не удалось создать директорию для хранения загруженных файлов.", ex);
        }
    }

    /**
     * Загружает массив файлов в хранилище и связывает их с задачей.
     * Выполняет валидацию каждого файла перед загрузкой.
     *
     * @param taskId идентификатор задачи
     * @param files массив файлов для загрузки
     * @return ResponseEntity со списком информации о загруженных файлах
     * @throws InvalidFileException если файлы не выбраны или не проходят валидацию
     * @throws FileStorageException если произошла ошибка при сохранении файлов
     * @throws TaskNotFoundException если задача не найдена
     */
    public ResponseEntity<List<Map<String, String>>> uploadFiles(Long taskId, MultipartFile[] files) {
        log.info("Получен запрос на загрузку {} файлов для задачи {}", files.length, taskId);
        
        if (files == null || files.length == 0) {
            throw new InvalidFileException("Не выбраны файлы для загрузки");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена: " + taskId));

        List<Map<String, String>> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);
            log.info("Загрузка файла: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
            String fileName = storeFile(file);

            // Сохраняем информацию о файле в базе данных
            TaskAttachment attachment = TaskAttachment.builder()
                    .task(task)
                    .filePath(fileName)
                    .build();
            taskAttachmentRepository.save(attachment);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/download/")
                    .path(fileName)
                    .toUriString();

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("fileDownloadUri", fileDownloadUri);
            response.put("fileType", file.getContentType());
            response.put("size", String.valueOf(file.getSize()));

            responses.add(response);
        }

        log.info("Успешно загружено файлов: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Скачивает файл по его имени.
     *
     * @param fileName имя файла для скачивания
     * @return ResponseEntity с файлом для скачивания
     * @throws InvalidFileException если имя файла пустое
     * @throws FileNotFoundException если файл не найден
     */
    public ResponseEntity<Resource> downloadFile(String fileName) {
        log.info("Получен запрос на скачивание файла: {}", fileName);
        
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new InvalidFileException("Имя файла не может быть пустым");
        }

        Resource resource = loadFileAsResource(fileName);
        // Определяем Content-Type по расширению файла
        String contentType = getFileType(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
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
    public ResponseEntity<Void> deleteFile(String fileName) {
        log.info("Получен запрос на удаление файла: {}", fileName);
        
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new InvalidFileException("Имя файла не может быть пустым");
        }

        // Удаляем запись из базы данных
        taskAttachmentRepository.findByFilePath(fileName)
                .ifPresent(taskAttachmentRepository::delete);

        deleteFileInternal(fileName);
        log.info("Файл {} удалён (если существовал)", fileName);
        return ResponseEntity.ok().build();
    }

    /**
     * Получает список файлов для конкретной задачи.
     *
     * @param taskId идентификатор задачи
     * @return ResponseEntity со списком информации о файлах задачи
     * @throws TaskNotFoundException если задача не найдена
     */
    public ResponseEntity<List<Map<String, String>>> getTaskFiles(Long taskId) {
        log.info("Получение списка файлов для задачи {}", taskId);
        
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException("Задача не найдена: " + taskId);
        }

        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(taskId);
        List<Map<String, String>> files = new ArrayList<>();

        for (TaskAttachment attachment : attachments) {
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/download/")
                    .path(attachment.getFilePath())
                    .toUriString();

            Map<String, String> fileInfo = new HashMap<>();
            fileInfo.put("fileName", attachment.getFilePath());
            fileInfo.put("fileDownloadUri", fileDownloadUri);
            fileInfo.put("uploadedAt", attachment.getUploadedAt().toString());
            
            // Получаем тип файла из расширения
            String fileType = getFileType(attachment.getFilePath());
            fileInfo.put("fileType", fileType);
            
            // Получаем размер файла
            try {
                Path filePath = fileStorageLocation.resolve(attachment.getFilePath());
                long size = Files.size(filePath);
                fileInfo.put("size", String.valueOf(size));
            } catch (IOException e) {
                log.warn("Не удалось получить размер файла {}: {}", attachment.getFilePath(), e.getMessage());
                fileInfo.put("size", "0");
            }

            files.add(fileInfo);
        }

        log.info("Найдено {} файлов для задачи {}", files.size(), taskId);
        return ResponseEntity.ok(files);
    }

    /**
     * Определяет тип файла по его расширению.
     *
     * @param fileName имя файла
     * @return MIME-тип файла
     */
    private String getFileType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }

    /**
     * Валидирует файл перед загрузкой.
     * Проверяет размер, тип и имя файла.
     *
     * @param file файл для валидации
     * @throws InvalidFileException если файл не проходит валидацию
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Файл пустой");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("Размер файла превышает максимально допустимый (10MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidFileException("Недопустимый тип файла. Разрешены: PDF, DOC, DOCX, XLS, XLSX, JPEG, PNG, GIF");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new InvalidFileException("Имя файла содержит недопустимую последовательность: " + originalFileName);
        }
    }

    /**
     * Сохраняет файл в хранилище.
     * Генерирует уникальное имя файла на основе UUID.
     *
     * @param file файл для сохранения
     * @return имя сохраненного файла
     * @throws FileStorageException если произошла ошибка при сохранении файла
     */
    private String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Не удалось сохранить файл " + fileName + ". Пожалуйста, попробуйте ещё раз!", ex);
        }
    }

    /**
     * Загружает файл как ресурс по его имени.
     *
     * @param fileName имя файла
     * @return ресурс файла
     * @throws FileNotFoundException если файл не найден
     */
    private Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("Файл не найден: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("Файл не найден: " + fileName, ex);
        }
    }

    /**
     * Удаляет файл из хранилища.
     *
     * @param fileName имя файла для удаления
     * @throws FileNotFoundException если файл не найден
     * @throws FileStorageException если произошла ошибка при удалении файла
     */
    private void deleteFileInternal(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (!Files.exists(filePath)) {
                throw new FileNotFoundException("Файл не найден: " + fileName);
            }
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Не удалось удалить файл " + fileName, ex);
        }
    }
} 
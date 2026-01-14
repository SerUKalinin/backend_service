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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

/**
 * Сервис для работы с файлами вложений задач.
 *
 * <p>Отвечает за загрузку, хранение, получение и удаление файлов,
 * а также за управление их метаданными в базе данных.</p>
 *
 * <p>Основная ответственность:</p>
 * <ul>
 *     <li>Валидация загружаемых файлов (размер, формат)</li>
 *     <li>Физическое сохранение файлов в файловой системе</li>
 *     <li>Создание и хранение метаданных файлов</li>
 *     <li>Генерация ссылок для скачивания файлов</li>
 *     <li>Удаление файлов и связанных метаданных</li>
 * </ul>
 *
 * <p>Сервис не реализует бизнес-логику задач и работает только с файловыми операциями.</p>
 */
@Slf4j
@Service
public class FileStorageService {

    /** Корневая директория для хранения файлов. */
    private final Path fileStorageLocation;

    /** Репозиторий для работы с задачами. */
    private final TaskRepository taskRepository;

    /** Репозиторий для работы с вложениями задач. */
    private final TaskAttachmentRepository taskAttachmentRepository;

    /** Максимальный размер файла в байтах (10 MB). */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /** Допустимые расширения файлов. */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png", "gif"
    );

    /**
     * Конструктор сервиса инициализирует директорию для хранения файлов.
     *
     * @param config конфигурация директории загрузки
     * @param taskRepository репозиторий задач
     * @param taskAttachmentRepository репозиторий вложений задач
     * @throws FileStorageException если не удалось создать директорию хранения
     */
    public FileStorageService(FileStorageConfig config,
                              TaskRepository taskRepository,
                              TaskAttachmentRepository taskAttachmentRepository) {
        this.fileStorageLocation = Paths.get(config.getUploadDir())
                .toAbsolutePath()
                .normalize();
        this.taskRepository = taskRepository;
        this.taskAttachmentRepository = taskAttachmentRepository;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Не удалось создать директорию для хранения файлов", e);
        }
    }

    /**
     * Загружает и сохраняет файлы, привязывая их к задаче.
     *
     * <p>Метод выполняет проверку существования задачи, валидацию файлов,
     * сохранение в файловой системе, сохранение метаданных и формирование ссылок для скачивания.</p>
     *
     * @param taskId идентификатор задачи, к которой привязываются файлы
     * @param files массив файлов для загрузки
     * @return список метаданных загруженных файлов с ссылками для скачивания
     * @throws TaskNotFoundException если задача с указанным ID не найдена
     * @throws InvalidFileException если файл невалиден (пустой, большой или недопустимого типа)
     * @throws FileStorageException при ошибках сохранения файла
     */
    public ResponseEntity<List<Map<String, String>>> uploadFiles(Long taskId, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new InvalidFileException("Файлы не выбраны");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена: " + taskId));

        List<Map<String, String>> responses = Arrays.stream(files)
                .peek(this::validateFile)
                .map(file -> {
                    String ext = Optional.ofNullable(StringUtils.getFilenameExtension(file.getOriginalFilename()))
                            .map(String::toLowerCase)
                            .orElse("");
                    String storedFileName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

                    try {
                        Files.copy(file.getInputStream(),
                                fileStorageLocation.resolve(storedFileName),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new FileStorageException("Ошибка при сохранении файла: " + file.getOriginalFilename(), e);
                    }

                    TaskAttachment attachment = TaskAttachment.builder()
                            .task(task)
                            .filePath(storedFileName)
                            .originalFileName(Optional.ofNullable(file.getOriginalFilename()).orElse("unknown"))
                            .size(file.getSize())
                            .build();
                    taskAttachmentRepository.save(attachment);

                    String fileUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/api/files/download/")
                            .path(storedFileName)
                            .toUriString();

                    log.info("Файл {} сохранён как {}", file.getOriginalFilename(), storedFileName);

                    return buildFileInfo(attachment, fileUri);
                })
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Получает список файлов, прикреплённых к задаче.
     *
     * @param taskId идентификатор задачи
     * @return список метаданных файлов с ссылками для скачивания
     * @throws TaskNotFoundException если задача с указанным ID не найдена
     */
    public ResponseEntity<List<Map<String, String>>> getTaskFiles(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException("Задача не найдена: " + taskId);
        }

        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(taskId);
        List<Map<String, String>> files = new ArrayList<>();

        for (TaskAttachment a : attachments) {
            String fileUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/download/")
                    .path(a.getFilePath())
                    .toUriString();

            Map<String, String> info = new HashMap<>();
            info.put("fileName", a.getFilePath());
            info.put("originalFileName", a.getOriginalFileName());
            info.put("fileDownloadUri", fileUri);
            info.put("fileType", getFileType(a.getFilePath()));
            info.put("uploadedAt", a.getUploadedAt().toString());

            try {
                info.put("size", String.valueOf(Files.size(fileStorageLocation.resolve(a.getFilePath()))));
            } catch (IOException e) {
                info.put("size", "0");
            }

            files.add(info);
        }

        return ResponseEntity.ok(files);
    }

    /**
     * Загружает файл как {@link Resource} для скачивания.
     *
     * @param fileName имя файла в хранилище
     * @return HTTP-ответ с файлом и корректным MIME-типом
     * @throws FileNotFoundException если файл не найден
     */
    public ResponseEntity<Resource> downloadFile(String fileName) {
        Resource resource = loadFileAsResource(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(getFileType(fileName)))
                .header("Content-Disposition",
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Удаляет файл и связанные с ним метаданные из системы.
     *
     * @param fileName имя файла в хранилище
     * @throws FileNotFoundException если файл не найден
     * @throws FileStorageException при ошибках удаления
     */
    public void deleteFile(String fileName) {
        taskAttachmentRepository.findByFilePath(fileName)
                .ifPresent(taskAttachmentRepository::delete);
        deleteFileInternal(fileName);
    }

    /* ==================== Вспомогательные методы ==================== */

    /**
     * Валидирует файл на предмет пустоты, размера и допустимого расширения.
     *
     * @param file файл для проверки
     * @throws InvalidFileException если файл пустой, превышает размер или недопустимого типа
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Файл пустой");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("Файл слишком большой");
        }

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new InvalidFileException("Недопустимый тип файла");
        }
    }

    /**
     * Формирует информацию о файле для ответа клиенту.
     *
     * @param attachment объект {@link TaskAttachment} с метаданными файла
     * @param fileUri URI для скачивания файла
     * @return Map с ключами: fileName, originalFileName, fileDownloadUri, fileType, size
     */
    private Map<String, String> buildFileInfo(TaskAttachment attachment, String fileUri) {
        Map<String, String> info = new HashMap<>();
        info.put("fileName", attachment.getFilePath());
        info.put("originalFileName", attachment.getOriginalFileName());
        info.put("fileDownloadUri", fileUri);
        info.put("fileType", getFileType(attachment.getFilePath()));
        info.put("size", String.valueOf(attachment.getSize()));
        return info;
    }

    /**
     * Загружает файл как ресурс.
     *
     * @param fileName имя файла
     * @return {@link Resource} для скачивания
     * @throws FileNotFoundException если файл не найден
     */
    private Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new FileNotFoundException("Файл не найден: " + fileName);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Файл не найден: " + fileName, e);
        }
    }

    /**
     * Удаляет файл из файловой системы.
     *
     * @param fileName имя файла
     * @throws FileNotFoundException если файл не найден
     * @throws FileStorageException при ошибках удаления
     */
    private void deleteFileInternal(String fileName) {
        try {
            Path path = fileStorageLocation.resolve(fileName).normalize();
            if (!Files.exists(path)) {
                throw new FileNotFoundException("Файл не найден: " + fileName);
            }
            Files.delete(path);
        } catch (IOException e) {
            throw new FileStorageException("Не удалось удалить файл", e);
        }
    }

    /**
     * Определяет MIME-тип файла по расширению.
     *
     * @param fileName имя файла
     * @return MIME-тип файла
     */
    private String getFileType(String fileName) {
        String ext = StringUtils.getFilenameExtension(fileName);
        if (ext == null) {
            return "application/octet-stream";
        }

        return switch (ext.toLowerCase()) {
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
}

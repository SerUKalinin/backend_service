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
 * Сервис для хранения, получения и удаления файлов, связанных с задачами.
 *
 * <p>Отвечает за загрузку файлов, валидацию формата и размера,
 * физическое сохранение в файловой системе, а также управление
 * метаданными файлов в базе данных.</p>
 *
 * <p><strong>Ответственность:</strong></p>
 * <ul>
 *   <li>Загрузка и валидация файлов вложений задач</li>
 *   <li>Хранение файлов в файловой системе</li>
 *   <li>Связывание файлов с задачами</li>
 *   <li>Формирование ссылок на скачивание</li>
 *   <li>Удаление файлов и их метаданных</li>
 * </ul>
 *
 * <p>Сервис не содержит бизнес-логики задач и работает исключительно
 * с файловыми операциями и их привязкой к сущностям.</p>
 */
@Slf4j
@Service
public class FileStorageService {

    /** Корневая директория для хранения загружаемых файлов. */
    private final Path fileStorageLocation;

    /** Репозиторий для работы с задачами. */
    private final TaskRepository taskRepository;

    /** Репозиторий для работы с вложениями задач. */
    private final TaskAttachmentRepository taskAttachmentRepository;

    /** Максимально допустимый размер файла (10 MB). */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /** Допустимые расширения файлов. */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png", "gif"
    );

    /**
     * Создаёт сервис хранения файлов и инициализирует директорию загрузки.
     *
     * @param config конфигурация хранилища файлов
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
     * Загружает файлы и привязывает их к задаче.
     *
     * <p>В процессе загрузки:</p>
     * <ol>
     *     <li>Проверяется существование задачи</li>
     *     <li>Валидируется каждый файл (размер, расширение)</li>
     *     <li>Файл сохраняется в файловой системе</li>
     *     <li>Метаданные сохраняются в базе данных</li>
     *     <li>Формируется ссылка для скачивания</li>
     * </ol>
     *
     * @param taskId идентификатор задачи
     * @param files массив файлов для загрузки
     * @return список информации о загруженных файлах
     * @throws TaskNotFoundException если задача не найдена
     * @throws InvalidFileException если файл невалиден
     * @throws FileStorageException при ошибке сохранения файла
     */
    public ResponseEntity<List<Map<String, String>>> uploadFiles(Long taskId, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new InvalidFileException("Файлы не выбраны");
        }

        // Проверяем существование задачи
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена: " + taskId));

        List<Map<String, String>> responses = Arrays.stream(files)
                .peek(this::validateFile) // Валидируем каждый файл
                .map(file -> {
                    // Генерация безопасного имени файла
                    String ext = Optional.ofNullable(StringUtils.getFilenameExtension(file.getOriginalFilename()))
                            .map(String::toLowerCase)
                            .orElse("");
                    String storedFileName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

                    // Сохраняем файл
                    try {
                        Files.copy(file.getInputStream(),
                                fileStorageLocation.resolve(storedFileName),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new FileStorageException("Ошибка при сохранении файла: " + file.getOriginalFilename(), e);
                    }

                    // Сохраняем метаданные
                    TaskAttachment attachment = TaskAttachment.builder()
                            .task(task)
                            .filePath(storedFileName)
                            .originalFileName(Optional.ofNullable(file.getOriginalFilename()).orElse("unknown"))
                            .size(file.getSize())
                            .build();
                    taskAttachmentRepository.save(attachment);

                    // Формируем ссылку для скачивания
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
     * Формирует информацию о файле для ответа клиенту.
     *
     * @param attachment объект TaskAttachment с метаданными файла
     * @param fileUri URI для скачивания файла
     * @return Map с данными файла
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
     * Возвращает список файлов, прикреплённых к задаче.
     *
     * @param taskId идентификатор задачи
     * @return список метаданных файлов
     * @throws TaskNotFoundException если задача не найдена
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
                info.put("size", String.valueOf(
                        Files.size(fileStorageLocation.resolve(a.getFilePath()))
                ));
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
     * @return файл в виде HTTP-ответа
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
     * Удаляет файл и связанные с ним метаданные из базы и файловой системы.
     *
     * @param fileName имя файла в хранилище
     * @throws FileNotFoundException если файл не найден
     * @throws FileStorageException при ошибке удаления
     */
    public void deleteFile(String fileName) {
        taskAttachmentRepository.findByFilePath(fileName)
                .ifPresent(taskAttachmentRepository::delete);
        deleteFileInternal(fileName);
    }

    /* ==================== Internal helpers ==================== */

    /**
     * Валидирует файл на предмет размера и допустимого расширения.
     *
     * @param file файл для проверки
     * @throws InvalidFileException если файл пустой, слишком большой или недопустимого типа
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
     * Сохраняет файл в файловую систему.
     *
     * @param file файл для сохранения
     * @return сгенерированное уникальное имя файла
     * @throws FileStorageException при ошибке сохранения
     */
    private String storeFile(MultipartFile file) {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + ext;

        try {
            Files.copy(
                    file.getInputStream(),
                    fileStorageLocation.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING
            );
            return fileName;
        } catch (IOException e) {
            throw new FileStorageException("Ошибка при сохранении файла", e);
        }
    }

    /**
     * Загружает файл из файловой системы как {@link Resource}.
     *
     * @param fileName имя файла
     * @return ресурс для скачивания
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
     * @throws FileStorageException при ошибке удаления
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
            case "docx" ->
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" ->
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }
}

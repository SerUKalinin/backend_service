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

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final TaskRepository taskRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png", "gif"
    );

    public FileStorageService(FileStorageConfig config,
                              TaskRepository taskRepository,
                              TaskAttachmentRepository taskAttachmentRepository) {
        this.fileStorageLocation = Paths.get(config.getUploadDir()).toAbsolutePath().normalize();
        this.taskRepository = taskRepository;
        this.taskAttachmentRepository = taskAttachmentRepository;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Не удалось создать директорию для хранения файлов", e);
        }
    }

    public ResponseEntity<List<Map<String, String>>> uploadFiles(Long taskId, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new InvalidFileException("Файлы не выбраны");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена: " + taskId));

        List<Map<String, String>> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);
            String storedFileName = storeFile(file);

            TaskAttachment attachment = TaskAttachment.builder()
                    .task(task)
                    .filePath(storedFileName)
                    .originalFileName(file.getOriginalFilename())
                    .size(file.getSize())
                    .build();
            taskAttachmentRepository.save(attachment);

            String fileUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/download/")
                    .path(storedFileName)
                    .toUriString();

            Map<String, String> info = new HashMap<>();
            info.put("fileName", storedFileName);
            info.put("originalFileName", file.getOriginalFilename());
            info.put("fileDownloadUri", fileUri);
            info.put("fileType", file.getContentType());
            info.put("size", String.valueOf(file.getSize()));

            responses.add(info);
        }

        return ResponseEntity.ok(responses);
    }

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

    public ResponseEntity<Resource> downloadFile(String fileName) {
        Resource resource = loadFileAsResource(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(getFileType(fileName)))
                .header("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    public void deleteFile(String fileName) {
        taskAttachmentRepository.findByFilePath(fileName)
                .ifPresent(taskAttachmentRepository::delete);
        deleteFileInternal(fileName);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new InvalidFileException("Файл пустой");
        if (file.getSize() > MAX_FILE_SIZE) throw new InvalidFileException("Файл слишком большой");

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new InvalidFileException("Недопустимый тип файла");
        }
    }

    private String storeFile(MultipartFile file) {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + ext;

        try {
            Files.copy(file.getInputStream(), fileStorageLocation.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new FileStorageException("Ошибка при сохранении файла", e);
        }
    }

    private Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) throw new FileNotFoundException("Файл не найден: " + fileName);
            return resource;
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Файл не найден: " + fileName, e);
        }
    }

    private void deleteFileInternal(String fileName) {
        try {
            Path path = fileStorageLocation.resolve(fileName).normalize();
            if (!Files.exists(path)) throw new FileNotFoundException("Файл не найден: " + fileName);
            Files.delete(path);
        } catch (IOException e) {
            throw new FileStorageException("Не удалось удалить файл", e);
        }
    }

    private String getFileType(String fileName) {
        String ext = StringUtils.getFilenameExtension(fileName);
        if (ext == null) return "application/octet-stream";

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

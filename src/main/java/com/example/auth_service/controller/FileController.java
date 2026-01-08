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

@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<List<Map<String, String>>> uploadFiles(
            @RequestParam Long taskId,
            @RequestParam MultipartFile[] files) {
        return fileStorageService.uploadFiles(taskId, files);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<Map<String, String>>> getTaskFiles(@PathVariable Long taskId) {
        return fileStorageService.getTaskFiles(taskId);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        return fileStorageService.downloadFile(fileName);
    }

    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName);
        return ResponseEntity.ok().build();
    }
}

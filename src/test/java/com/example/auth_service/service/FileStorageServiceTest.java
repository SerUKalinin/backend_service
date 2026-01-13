package com.example.auth_service.service;

import com.example.auth_service.config.FileStorageConfig;
import com.example.auth_service.exception.*;
import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskAttachment;
import com.example.auth_service.repository.TaskAttachmentRepository;
import com.example.auth_service.repository.TaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.io.File;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    private TaskRepository taskRepository;
    private TaskAttachmentRepository taskAttachmentRepository;
    private FileStorageService fileStorageService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        // Моки репозиториев
        taskRepository = mock(TaskRepository.class);
        taskAttachmentRepository = mock(TaskAttachmentRepository.class);

        // Временная директория для файлов
        tempDir = Files.createTempDirectory("test-files");

        // Мок конфигурации хранилища
        FileStorageConfig config = mock(FileStorageConfig.class);
        when(config.getUploadDir()).thenReturn(tempDir.toString());

        fileStorageService = new FileStorageService(config, taskRepository, taskAttachmentRepository);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setScheme("http");
        request.setContextPath("/api");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() throws Exception {
        RequestContextHolder.resetRequestAttributes();

        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        }

    @Test
    void uploadFiles_shouldThrowTaskNotFound_ifTaskDoesNotExist() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> fileStorageService.uploadFiles(1L, new MockMultipartFile[]{file}));
    }

    @Test
    void uploadFiles_shouldThrowInvalidFile_ifEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
        Task task = new Task();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(InvalidFileException.class, () -> fileStorageService.uploadFiles(1L, new MockMultipartFile[]{file}));
    }

    @Test
    void uploadFiles_shouldSaveFileSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "content".getBytes()
        );

        Task task = new Task();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        ResponseEntity<List<Map<String, String>>> response =
                fileStorageService.uploadFiles(1L, new MockMultipartFile[]{file});

        assertEquals(1, response.getBody().size());

        ArgumentCaptor<TaskAttachment> captor = ArgumentCaptor.forClass(TaskAttachment.class);
        verify(taskAttachmentRepository).save(captor.capture());

        TaskAttachment saved = captor.getValue();
        assertEquals(task, saved.getTask());
        assertEquals("test.pdf", saved.getOriginalFileName());
    }


    @Test
    void downloadFile_shouldThrowFileNotFound_ifFileDoesNotExist() {
        assertThrows(FileNotFoundException.class, () -> fileStorageService.downloadFile("nonexistent.pdf"));
    }
}

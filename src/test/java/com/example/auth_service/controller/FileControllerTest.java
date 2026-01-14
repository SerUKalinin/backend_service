package com.example.auth_service.controller;

import com.example.auth_service.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileControllerTest {

    private FileStorageService fileStorageService;
    private FileController fileController;

    @BeforeEach
    void setUp() {
        fileStorageService = mock(FileStorageService.class);
        fileController = new FileController(fileStorageService);
    }

    @Test
    @DisplayName("Загрузка файлов: вызов сервиса и возврат информации о файлах")
    void uploadFiles_shouldCallServiceAndReturnResponse() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        Long taskId = 1L;

        List<Map<String, String>> mockResponse = List.of(Map.of("fileName", "test.pdf"));
        when(fileStorageService.uploadFiles(taskId, new MultipartFile[]{file})).thenReturn(ResponseEntity.ok(mockResponse));

        ResponseEntity<List<Map<String, String>>> response = fileController.uploadFiles(taskId, new MultipartFile[]{file});

        assertNotNull(response);
        assertEquals(1, response.getBody().size());
        assertEquals("test.pdf", response.getBody().get(0).get("fileName"));
        verify(fileStorageService).uploadFiles(taskId, new MultipartFile[]{file});
    }

    @Test
    @DisplayName("Получение списка файлов задачи: возвращает файлы из сервиса")
    void getTaskFiles_shouldReturnFilesFromService() {
        Long taskId = 1L;
        List<Map<String, String>> mockResponse = List.of(Map.of("fileName", "file1.txt"));
        when(fileStorageService.getTaskFiles(taskId)).thenReturn(ResponseEntity.ok(mockResponse));

        ResponseEntity<List<Map<String, String>>> response = fileController.getTaskFiles(taskId);

        assertNotNull(response);
        assertEquals(1, response.getBody().size());
        assertEquals("file1.txt", response.getBody().get(0).get("fileName"));
        verify(fileStorageService).getTaskFiles(taskId);
    }

    @Test
    @DisplayName("Скачивание файла: возвращает ресурс с содержимым файла")
    void downloadFile_shouldReturnResource() {
        String fileName = "file.txt";
        Resource resource = new ByteArrayResource("content".getBytes());
        when(fileStorageService.downloadFile(fileName)).thenReturn(ResponseEntity.ok(resource));

        ResponseEntity<Resource> response = fileController.downloadFile(fileName);

        assertNotNull(response);
        assertEquals(resource, response.getBody());
        verify(fileStorageService).downloadFile(fileName);
    }

    @Test
    @DisplayName("Удаление файла: вызов сервиса и возврат статуса 200")
    void deleteFile_shouldCallService() {
        String fileName = "file.txt";

        ResponseEntity<Void> response = fileController.deleteFile(fileName);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(fileStorageService).deleteFile(fileName);
    }
}

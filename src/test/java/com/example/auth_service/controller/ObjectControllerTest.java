package com.example.auth_service.controller;

import com.example.auth_service.dto.ObjectRequestDto;
import com.example.auth_service.dto.ObjectResponseDto;
import com.example.auth_service.model.ObjectType;
import com.example.auth_service.service.ObjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObjectControllerTest {

    private ObjectService objectService;
    private ObjectController objectController;

    @BeforeEach
    void setUp() {
        objectService = mock(ObjectService.class);
        objectController = new ObjectController(objectService);
    }

    @Test
    void getAllObjects_shouldReturnList() {
        List<ObjectResponseDto> mockList = List.of(new ObjectResponseDto());
        when(objectService.getAllObjects()).thenReturn(mockList);

        ResponseEntity<List<ObjectResponseDto>> response = objectController.getAllObjects();

        assertEquals(mockList, response.getBody());
        verify(objectService).getAllObjects();
    }

    @Test
    void getObjectById_shouldReturnObject() {
        ObjectResponseDto dto = new ObjectResponseDto();
        when(objectService.getObjectById(1L)).thenReturn(dto);

        ResponseEntity<ObjectResponseDto> response = objectController.getObjectById(1L);

        assertEquals(dto, response.getBody());
        verify(objectService).getObjectById(1L);
    }

    @Test
    void getObjectsByType_shouldReturnList() {
        List<ObjectResponseDto> mockList = List.of(new ObjectResponseDto());
        when(objectService.getObjectsByType(ObjectType.BUILDING)).thenReturn(mockList);

        ResponseEntity<List<ObjectResponseDto>> response = objectController.getObjectsByType(ObjectType.BUILDING);

        assertEquals(mockList, response.getBody());
        verify(objectService).getObjectsByType(ObjectType.BUILDING);
    }

    @Test
    void getChildren_shouldReturnList() {
        List<ObjectResponseDto> mockList = List.of(new ObjectResponseDto());
        when(objectService.getChildren(1L)).thenReturn(mockList);

        ResponseEntity<List<ObjectResponseDto>> response = objectController.getChildren(1L);

        assertEquals(mockList, response.getBody());
        verify(objectService).getChildren(1L);
    }

    @Test
    void createObject_shouldReturnCreatedObject() {
        ObjectRequestDto request = new ObjectRequestDto();
        ObjectResponseDto responseDto = new ObjectResponseDto();
        when(objectService.createObject(request)).thenReturn(responseDto);

        ResponseEntity<ObjectResponseDto> response = objectController.createObject(request);

        assertEquals(responseDto, response.getBody());
        verify(objectService).createObject(request);
    }

    @Test
    void updateObject_shouldReturnUpdatedObject() {
        ObjectRequestDto request = new ObjectRequestDto();
        ObjectResponseDto responseDto = new ObjectResponseDto();
        when(objectService.updateObject(1L, request)).thenReturn(responseDto);

        ResponseEntity<ObjectResponseDto> response = objectController.updateObject(1L, request);

        assertEquals(responseDto, response.getBody());
        verify(objectService).updateObject(1L, request);
    }

    @Test
    void deleteObject_shouldCallService() {
        ResponseEntity<Void> response = objectController.deleteObject(1L);

        assertEquals(204, response.getStatusCodeValue());
        verify(objectService).deleteObject(1L);
    }

    @Test
    void getCurrentUserObjects_shouldReturnList() {
        List<ObjectResponseDto> mockList = List.of(new ObjectResponseDto());
        when(objectService.getCurrentUserObjects()).thenReturn(mockList);

        ResponseEntity<List<ObjectResponseDto>> response = objectController.getCurrentUserObjects();

        assertEquals(mockList, response.getBody());
        verify(objectService).getCurrentUserObjects();
    }

    @Test
    void getObjectsByResponsibleUser_shouldReturnList() {
        List<ObjectResponseDto> mockList = List.of(new ObjectResponseDto());
        when(objectService.getObjectsByResponsibleUser(2L)).thenReturn(mockList);

        ResponseEntity<List<ObjectResponseDto>> response = objectController.getObjectsByResponsibleUser(2L);

        assertEquals(mockList, response.getBody());
        verify(objectService).getObjectsByResponsibleUser(2L);
    }

    @Test
    void assignResponsibleUser_shouldReturnUpdatedObject() {
        ObjectResponseDto responseDto = new ObjectResponseDto();
        when(objectService.assignResponsibleUser(1L, 2L)).thenReturn(responseDto);

        ResponseEntity<ObjectResponseDto> response = objectController.assignResponsibleUser(1L, 2L);

        assertEquals(responseDto, response.getBody());
        verify(objectService).assignResponsibleUser(1L, 2L);
    }

    @Test
    void removeResponsibleUser_shouldReturnUpdatedObject() {
        ObjectResponseDto responseDto = new ObjectResponseDto();
        when(objectService.removeResponsibleUser(1L)).thenReturn(responseDto);

        ResponseEntity<ObjectResponseDto> response = objectController.removeResponsibleUser(1L);

        assertEquals(responseDto, response.getBody());
        verify(objectService).removeResponsibleUser(1L);
    }

    @Test
    void getObjectPath_shouldReturnList() {
        List<ObjectResponseDto> mockList = List.of(new ObjectResponseDto());
        when(objectService.getObjectPath(1L)).thenReturn(mockList);

        ResponseEntity<List<ObjectResponseDto>> response = objectController.getObjectPath(1L);

        assertEquals(mockList, response.getBody());
        verify(objectService).getObjectPath(1L);
    }
}

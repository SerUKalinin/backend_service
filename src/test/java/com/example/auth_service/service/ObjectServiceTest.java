package com.example.auth_service.service;

import com.example.auth_service.dto.ObjectRequestDto;
import com.example.auth_service.dto.ObjectResponseDto;
import com.example.auth_service.exception.ObjectNotFoundException;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.mapper.ObjectMapper;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.ObjectType;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.ObjectRepository;
import com.example.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ObjectServiceTest {

    private ObjectRepository objectRepository;
    private UserRepository userRepository;
    private ObjectMapper objectMapper;
    private ObjectService objectService;

    @BeforeEach
    void setUp() {
        objectRepository = mock(ObjectRepository.class);
        userRepository = mock(UserRepository.class);
        objectMapper = mock(ObjectMapper.class);

        objectService = new ObjectService(objectRepository, userRepository, objectMapper);
    }

    @Test
    void createObject_shouldSaveObjectSuccessfully() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("user");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(currentUser));

        ObjectRequestDto dto = new ObjectRequestDto();
        dto.setName("New Object");
        dto.setObjectType(ObjectType.BUILDING);

        ObjectEntity entity = new ObjectEntity();
        entity.setName(dto.getName());
        entity.setObjectType(dto.getObjectType());

        ObjectEntity savedEntity = new ObjectEntity();
        savedEntity.setId(10L);
        savedEntity.setName(dto.getName());
        savedEntity.setObjectType(dto.getObjectType());

        when(objectMapper.toEntity(dto)).thenReturn(entity);
        when(objectRepository.save(entity)).thenReturn(savedEntity);
        when(objectMapper.toDto(savedEntity)).thenReturn(new ObjectResponseDto());

        ObjectResponseDto response = objectService.createObject(dto);

        assertNotNull(response);
        verify(objectRepository).save(entity);
        assertEquals(currentUser, entity.getCreatedBy());
    }

    @Test
    void getObjectById_shouldThrowException_ifNotFound() {
        when(objectRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> objectService.getObjectById(99L));
    }

    @Test
    void deleteObject_shouldThrowException_ifHasChildren() {
        ObjectEntity parent = new ObjectEntity();
        when(objectRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(objectRepository.existsByParentId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> objectService.deleteObject(1L));
    }

    @Test
    void assignResponsibleUser_shouldUpdateSuccessfully() {
        ObjectEntity object = new ObjectEntity();
        when(objectRepository.findById(1L)).thenReturn(Optional.of(object));

        User responsible = new User();
        responsible.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(responsible));

        ObjectEntity saved = new ObjectEntity();
        when(objectRepository.save(object)).thenReturn(saved);
        when(objectMapper.toDto(saved)).thenReturn(new ObjectResponseDto());

        ObjectResponseDto dto = objectService.assignResponsibleUser(1L, 2L);

        assertNotNull(dto);
        assertEquals(responsible, object.getResponsibleUser());
        verify(objectRepository).save(object);
    }

    @Test
    void getObjectPath_shouldReturnFullPath() {
        ObjectEntity root = new ObjectEntity();
        root.setId(1L);
        ObjectEntity child = new ObjectEntity();
        child.setId(2L);
        child.setParent(root);

        when(objectRepository.findById(2L)).thenReturn(Optional.of(child));
        when(objectMapper.toDto(root)).thenReturn(new ObjectResponseDto());
        when(objectMapper.toDto(child)).thenReturn(new ObjectResponseDto());

        List<ObjectResponseDto> path = objectService.getObjectPath(2L);
        assertEquals(2, path.size());
    }
}

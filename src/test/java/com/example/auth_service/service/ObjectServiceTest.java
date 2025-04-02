package com.example.auth_service.service;

import com.example.auth_service.exception.ObjectNotFoundException;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.ObjectType;
import com.example.auth_service.repository.ObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ObjectServiceTest {

    @Mock
    private ObjectRepository objectRepository;

    @InjectMocks
    private ObjectService objectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Успешное создание объекта недвижимости")
    void createObject_Success() {
        ObjectEntity object = ObjectEntity.builder()
                .name("Квартира")
                .objectType(ObjectType.APARTMENT)
                .build();

        when(objectRepository.save(any(ObjectEntity.class))).thenReturn(object);

        ObjectEntity created = objectService.createObject(object);

        assertNotNull(created);
        assertEquals("Квартира", created.getName());
        verify(objectRepository, times(1)).save(any(ObjectEntity.class));
    }

    @Test
    @DisplayName("Ошибка при создании объекта: null объект")
    void createObject_Fail_NullObject() {
        assertThrows(IllegalArgumentException.class, () -> objectService.createObject(null));
    }

    @Test
    @DisplayName("Ошибка при создании объекта: пустое имя")
    void createObject_Fail_EmptyName() {
        ObjectEntity object = ObjectEntity.builder()
                .name("")  // Пустое имя
                .objectType(ObjectType.APARTMENT)
                .build();
        assertThrows(IllegalArgumentException.class, () -> objectService.createObject(object));
    }

    @Test
    @DisplayName("Получение всех объектов")
    void getAllObjects_Success() {
        List<ObjectEntity> objects = List.of(
                ObjectEntity.builder().id(1L).name("Дом").objectType(ObjectType.BUILDING).build(),
                ObjectEntity.builder().id(2L).name("Квартира").objectType(ObjectType.APARTMENT).build()
        );
        when(objectRepository.findAll()).thenReturn(objects);

        List<ObjectEntity> result = objectService.getAllObjects();

        assertEquals(2, result.size());
        verify(objectRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Получение объекта по ID: найден")
    void getObjectById_Found() {
        ObjectEntity object = ObjectEntity.builder()
                .id(1L)
                .name("Дом")
                .objectType(ObjectType.BUILDING)
                .build();
        when(objectRepository.findById(1L)).thenReturn(Optional.of(object));

        Optional<ObjectEntity> result = objectService.getObjectById(1L);

        assertTrue(result.isPresent());
        assertEquals("Дом", result.get().getName());
    }

    @Test
    @DisplayName("Получение объекта по ID: не найден")
    void getObjectById_NotFound() {
        when(objectRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<ObjectEntity> result = objectService.getObjectById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Обновление объекта: успех")
    void updateObject_Success() {
        ObjectEntity existing = ObjectEntity.builder()
                .id(1L)
                .name("Дом")
                .objectType(ObjectType.BUILDING)
                .build();
        ObjectEntity updated = ObjectEntity.builder()
                .id(1L)
                .name("Обновленный дом")
                .objectType(ObjectType.BUILDING)
                .build();
        when(objectRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(objectRepository.save(any(ObjectEntity.class))).thenReturn(updated);

        ObjectEntity result = objectService.updateObject(1L, updated);

        assertEquals("Обновленный дом", result.getName());
        verify(objectRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("Ошибка при обновлении: объект не найден")
    void updateObject_Fail_NotFound() {
        ObjectEntity updated = ObjectEntity.builder()
                .id(1L)
                .name("Обновленный дом")
                .objectType(ObjectType.BUILDING)
                .build();
        when(objectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> objectService.updateObject(1L, updated));
    }

    @Test
    @DisplayName("Ошибка при обновлении: пустое имя")
    void updateObject_Fail_EmptyName() {
        ObjectEntity updated = ObjectEntity.builder()
                .id(1L)
                .name("")  // Пустое имя
                .objectType(ObjectType.BUILDING)
                .build();
        when(objectRepository.findById(1L)).thenReturn(Optional.of(ObjectEntity.builder().id(1L).name("Дом").objectType(ObjectType.BUILDING).build()));

        assertThrows(IllegalArgumentException.class, () -> objectService.updateObject(1L, updated));
    }

    @Test
    @DisplayName("Удаление объекта: успех")
    void deleteObject_Success() {
        ObjectEntity object = ObjectEntity.builder()
                .id(1L)
                .name("Дом")
                .objectType(ObjectType.BUILDING)
                .build();
        when(objectRepository.findById(1L)).thenReturn(Optional.of(object));
        when(objectRepository.findByParentId(1L)).thenReturn(List.of());

        objectService.deleteObject(1L);

        verify(objectRepository, times(1)).delete(object);
    }

    @Test
    @DisplayName("Ошибка при удалении: объект не найден")
    void deleteObject_Fail_NotFound() {
        when(objectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> objectService.deleteObject(1L));
    }

    @Test
    @DisplayName("Ошибка при удалении: у объекта есть дочерние элементы")
    void deleteObject_Fail_HasChildren() {
        ObjectEntity object = ObjectEntity.builder()
                .id(1L)
                .name("Дом")
                .objectType(ObjectType.BUILDING)
                .build();
        ObjectEntity child = ObjectEntity.builder()
                .id(2L)
                .name("Квартира")
                .objectType(ObjectType.APARTMENT)
                .parent(object)
                .build();
        when(objectRepository.findById(1L)).thenReturn(Optional.of(object));
        when(objectRepository.findByParentId(1L)).thenReturn(List.of(child));

        assertThrows(IllegalStateException.class, () -> objectService.deleteObject(1L));
    }
}
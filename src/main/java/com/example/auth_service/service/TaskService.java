package com.example.auth_service.service;

import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.exception.TaskNotFoundException;
import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import com.example.auth_service.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления задачами.
 * Предоставляет функциональность для создания, получения, обновления и удаления задач.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    /**
     * Создание новой задачи.
     *
     * @param taskCreateDTO DTO объекта для создания задачи.
     * @return TaskDTO объект созданной задачи.
     */
    public TaskDTO createTask(TaskCreateDTO taskCreateDTO) {
        log.info("Создание задачи: {}", taskCreateDTO);

        Task task = modelMapper.map(taskCreateDTO, Task.class);
        task.setStatus(TaskStatus.NEW);  // Устанавливаем начальный статус задачи
        Task savedTask = taskRepository.save(task);

        return modelMapper.map(savedTask, TaskDTO.class);
    }

    /**
     * Получить все задачи.
     *
     * @return Список всех задач в формате TaskDTO.
     */
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(task -> modelMapper.map(task, TaskDTO.class))
                .toList();
    }

    /**
     * Получить задачу по её идентификатору.
     *
     * @param id Идентификатор задачи.
     * @return TaskDTO объект найденной задачи.
     * @throws TaskNotFoundException Если задача с указанным идентификатором не найдена.
     */
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена"));
        return modelMapper.map(task, TaskDTO.class);
    }

    /**
     * Обновить существующую задачу.
     *
     * @param id Идентификатор задачи.
     * @param taskUpdateDTO DTO с новыми данными для обновления задачи.
     * @return TaskDTO объект обновленной задачи.
     * @throws TaskNotFoundException Если задача с указанным идентификатором не найдена.
     */
    @Transactional
    public TaskDTO updateTask(Long id, TaskUpdateDTO taskUpdateDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена"));

        modelMapper.map(taskUpdateDTO, task);
        return modelMapper.map(taskRepository.save(task), TaskDTO.class);
    }

    /**
     * Удалить задачу по её идентификатору.
     *
     * @param id Идентификатор задачи.
     * @throws TaskNotFoundException Если задача с указанным идентификатором не найдена.
     */
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Задача не найдена");
        }
        taskRepository.deleteById(id);
    }
}

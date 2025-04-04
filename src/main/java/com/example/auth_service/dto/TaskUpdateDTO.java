package com.example.auth_service.dto;

import com.example.auth_service.model.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskUpdateDTO {

    @NotBlank
    @Size(min = 3, max = 255)
    private String title;

    @Size(max = 2000)
    private String description;

    private TaskStatus status;

    @FutureOrPresent
    private LocalDateTime deadline;
}

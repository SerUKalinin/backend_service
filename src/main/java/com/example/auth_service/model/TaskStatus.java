package com.example.auth_service.model;

/**
 * Статусы задачи.
 */
public enum TaskStatus {
    /** Новая задача. */
    NEW,

    /** В процессе выполнения. */
    IN_PROGRESS,

    /** Просроченная задача. */
    EXPIRED,

    /** Срочная задача. */
    URGENT,

    /** Завершённая задача. */
    COMPLETED
}

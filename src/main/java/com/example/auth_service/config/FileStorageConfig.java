package com.example.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный бин для параметров файлового хранилища.
 * <p>
 * Позволяет централизованно управлять настройками, связанными с загрузкой и хранением файлов.
 * Значения загружаются из файла конфигурации приложения (application.properties или application.yml)
 * с префиксом {@code file}.
 * </p>
 * <p>
 * Используется сервисами файлового хранилища для определения базового пути сохранения и организации работы с файлами.
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageConfig {

    /**
     * Базовая директория для загрузки файлов.
     * <p>
     * Определяет физический путь, куда будут сохраняться файлы, загруженные пользователями.
     * Не должно быть null или пустым, иначе сервисы загрузки файлов могут работать некорректно.
     */
    private String uploadDir;

    /**
     * Возвращает базовую директорию для загрузки файлов.
     * <p>
     * Используется сервисами для получения пути сохранения файлов и организации файловой структуры.
     *
     * @return путь к директории загрузки файлов, заданный в конфигурации приложения
     */
    public String getUploadDir() {
        return uploadDir;
    }

    /**
     * Устанавливает базовую директорию для загрузки файлов.
     * <p>
     * Значение передаётся из конфигурационного файла приложения или программно при инициализации.
     *
     * @param uploadDir путь к директории загрузки файлов; не должен быть null или пустым
     */
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}

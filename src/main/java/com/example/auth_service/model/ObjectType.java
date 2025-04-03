package com.example.auth_service.model;

/**
 * Перечисление типов объектов в системе.
 */
public enum ObjectType {
    /** Здание. */
    BUILDING,

    /** Подъезд. */
    ENTRANCE,

    /** Цокольный этаж. */
    BASEMENT_FLOOR,

    /** Этаж. */
    FLOOR,

    /** Лестничный пролет. */
    STAIRWELL,

    /** Лифт. */
    ELEVATOR,

    /** Балкон этажа. */
    FLOOR_BALCONY,

    /** Коридор. */
    CORRIDOR,

    /** Холл лифта. */
    ELEVATOR_HALL,

    /** Квартира. */
    APARTMENT,

    /** Балкон квартиры. */
    APARTMENT_BALCONY,

    /** Комната. */
    ROOM,

    /** Задача. */
    TASK
}

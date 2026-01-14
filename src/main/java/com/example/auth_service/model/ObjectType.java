package com.example.auth_service.model;

/**
 * Перечисление типов объектов в системе недвижимости.
 *
 * <p>Используется для указания типа {@link ObjectEntity} в иерархии объектов:
 * от проектов и зданий до этажей, квартир, комнат и задач.</p>
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

    /** Задача, связанная с объектом недвижимости. */
    TASK
}

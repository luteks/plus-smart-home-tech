package ru.yandex.practicum.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String e) {
        super(e);
    }
}
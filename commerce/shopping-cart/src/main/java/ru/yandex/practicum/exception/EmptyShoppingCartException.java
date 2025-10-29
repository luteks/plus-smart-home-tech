package ru.yandex.practicum.exception;

public class EmptyShoppingCartException extends RuntimeException {
    public EmptyShoppingCartException(String message) {
        super(message);
    }
}
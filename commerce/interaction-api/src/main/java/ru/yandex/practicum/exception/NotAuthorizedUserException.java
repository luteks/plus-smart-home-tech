package ru.yandex.practicum.exception;

public class NotAuthorizedUserException extends RuntimeException {
    public NotAuthorizedUserException(String ex) {
        super(ex);
    }
}
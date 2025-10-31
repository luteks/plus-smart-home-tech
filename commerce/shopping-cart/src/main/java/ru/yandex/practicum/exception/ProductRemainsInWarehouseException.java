package ru.yandex.practicum.exception;

public class ProductRemainsInWarehouseException extends RuntimeException {
    public ProductRemainsInWarehouseException(String message) {
        super(message);
    }
}
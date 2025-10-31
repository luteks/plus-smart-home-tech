package ru.yandex.practicum.exception;

public class ProductNotUniqueInWarehouseException extends RuntimeException {
    public ProductNotUniqueInWarehouseException(String message) {
        super(message);
    }
}

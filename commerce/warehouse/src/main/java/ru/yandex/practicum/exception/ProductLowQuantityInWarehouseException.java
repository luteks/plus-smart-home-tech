package ru.yandex.practicum.exception;

public class ProductLowQuantityInWarehouseException extends RuntimeException {
    public ProductLowQuantityInWarehouseException(String message) {
        super(message);
    }
}

package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.model.Warehouse;
import ru.yandex.practicum.request.RegistrationProductInWarehouseRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WarehouseMapper {
    Warehouse toWarehouse(RegistrationProductInWarehouseRequest registrationProductInWarehouseRequest);
}
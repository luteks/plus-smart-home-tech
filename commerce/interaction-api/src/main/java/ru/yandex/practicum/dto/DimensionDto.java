package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DimensionDto {
    @Min(1)
    private Double width;

    @Min(1)
    private Double height;

    @Min(1)
    private Double depth;
}
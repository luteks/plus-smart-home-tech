package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class ShoppingCartDto {
    @NotNull
    private UUID shoppingCartId;

    @NotNull
    @NotEmpty
    private Map<@NotNull UUID, @NotNull @Positive Integer> products;
}
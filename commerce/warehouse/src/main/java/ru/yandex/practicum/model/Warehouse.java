package ru.yandex.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "warehouse_product")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {
    @Id
    @Column(name = "product_id")
    private UUID productId;

    private Long quantity;

    private Boolean fragile;

    private Double width;

    private Double height;

    private Double depth;

    private double weight;

}
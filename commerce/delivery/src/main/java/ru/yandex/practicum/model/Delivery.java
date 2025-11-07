package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.DeliveryState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deliveryId;

    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private DeliveryState deliveryState;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryCost;

    @Column
    private Double deliveryWeight;

    @Column
    private Double deliveryVolume;

    @Column
    private Boolean fragile;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "from_address", joinColumns = @JoinColumn(name = "delivery_id"),
            inverseJoinColumns = @JoinColumn(name = "address_id"))
    private Address fromAddress;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "to_address", joinColumns = @JoinColumn(name = "delivery_id"),
            inverseJoinColumns = @JoinColumn(name = "address_id"))
    private Address toAddress;

}
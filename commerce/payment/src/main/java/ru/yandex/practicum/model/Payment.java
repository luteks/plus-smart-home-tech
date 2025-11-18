package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.PaymentState;

import java.util.UUID;

@Entity
@Table(name = "payments")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;
    private UUID orderId;

    private Double productsTotal;
    private Double deliveryTotal;
    private Double totalPayment;
    private Double feeTotal;

    @Enumerated(EnumType.STRING)
    private PaymentState status;
}
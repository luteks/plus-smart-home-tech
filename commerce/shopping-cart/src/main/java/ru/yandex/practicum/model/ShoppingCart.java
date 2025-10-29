package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.CartState;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shopping_cart")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "shopping_cart_id")
    private UUID shoppingCartId;

    @Column(name = "username")
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "cart_state")
    private CartState cartState;

    @ElementCollection
    @CollectionTable(name = "shopping_cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> products;
}
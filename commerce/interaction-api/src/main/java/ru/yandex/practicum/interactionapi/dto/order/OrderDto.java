package ru.yandex.practicum.interactionapi.dto.order;

import ru.yandex.practicum.interactionapi.enums.OrderState;
import ru.yandex.practicum.interactionapi.dto.warehouse.AddressDto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record OrderDto(
        UUID orderId,
        UUID shoppingCartId,
        Map<UUID, Long> products,
        UUID paymentId,
        UUID deliveryId,
        OrderState state,
        double deliveryWeight,
        double deliveryVolume,
        boolean fragile,
        BigDecimal totalPrice,
        BigDecimal deliveryPrice,
        BigDecimal productPrice,
        AddressDto fromAddress,
        AddressDto toAddress
) {
}
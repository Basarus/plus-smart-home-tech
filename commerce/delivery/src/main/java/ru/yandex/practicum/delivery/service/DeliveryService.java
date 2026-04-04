package ru.yandex.practicum.delivery.service;

import ru.yandex.practicum.interactionapi.dto.delivery.DeliveryDto;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryService {
    DeliveryDto planDelivery(DeliveryDto deliveryDto);

    BigDecimal deliveryCost(OrderDto orderDto);

    void deliveryPicked(UUID deliveryId);

    void deliverySuccessful(UUID deliveryId);

    void deliveryFailed(UUID deliveryId);
}
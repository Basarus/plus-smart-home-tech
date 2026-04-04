package ru.yandex.practicum.order.service;

import ru.yandex.practicum.interactionapi.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createNewOrder(CreateNewOrderRequest request);

    OrderDto payment(UUID orderId);

    OrderDto paymentSuccess(UUID orderId);

    OrderDto paymentFailed(UUID orderId);

    OrderDto delivery(UUID orderId);

    OrderDto deliveryFailed(UUID orderId);

    OrderDto assembly(UUID orderId);

    OrderDto assemblyFailed(UUID orderId);

    OrderDto productReturn(UUID orderId);

    BigDecimal calculateDeliveryCost(UUID orderId);

    BigDecimal calculateTotalCost(UUID orderId);

    List<OrderDto> getUserOrders(String username);

    OrderDto getOrder(UUID orderId);
}
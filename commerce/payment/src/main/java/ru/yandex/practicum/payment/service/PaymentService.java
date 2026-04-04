package ru.yandex.practicum.payment.service;

import ru.yandex.practicum.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.interactionapi.dto.payment.PaymentDto;

import java.util.UUID;

public interface PaymentService {
    PaymentDto payment(OrderDto orderDto);

    Double getTotalCost(OrderDto orderDto);

    Double productCost(OrderDto orderDto);

    void paymentSuccess(UUID paymentId);

    void paymentFailed(UUID paymentId);
}
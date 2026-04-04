package ru.yandex.practicum.interactionapi.dto.payment;

import ru.yandex.practicum.interactionapi.enums.PaymentState;

import java.util.UUID;

public record PaymentDto(
        UUID paymentId,
        Double totalPayment,
        Double deliveryTotal,
        Double feeTotal,
        Double productTotal,
        UUID orderId,
        PaymentState state
) {
}
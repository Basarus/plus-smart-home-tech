package ru.yandex.practicum.interactionapi.dto.payment;

import ru.yandex.practicum.interactionapi.enums.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentDto(
        UUID paymentId,
        BigDecimal totalPayment,
        BigDecimal deliveryTotal,
        BigDecimal feeTotal,
        BigDecimal productTotal,
        UUID orderId,
        PaymentState state
) {
}
package ru.yandex.practicum.payment.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interactionapi.api.PaymentOperations;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.interactionapi.dto.payment.PaymentDto;
import ru.yandex.practicum.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
public class PaymentController implements PaymentOperations {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public PaymentDto payment(@Valid OrderDto orderDto) {
        return paymentService.payment(orderDto);
    }

    @Override
    public BigDecimal getTotalCost(@Valid OrderDto orderDto) {
        return paymentService.getTotalCost(orderDto);
    }

    @Override
    public BigDecimal productCost(@Valid OrderDto orderDto) {
        return paymentService.productCost(orderDto);
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }
}
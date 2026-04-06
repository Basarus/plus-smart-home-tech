package ru.yandex.practicum.interactionapi.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.interactionapi.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

@RequestMapping("/api/v1/payment")
public interface PaymentOperations {

    @PostMapping("/productCost")
    BigDecimal productCost(@RequestBody OrderDto orderDto);

    @PostMapping("/totalCost")
    BigDecimal getTotalCost(@RequestBody OrderDto orderDto);

    @PostMapping
    PaymentDto payment(@RequestBody OrderDto orderDto);

    @PostMapping("/refund")
    void paymentSuccess(@RequestBody UUID paymentId);

    @PostMapping("/failed")
    void paymentFailed(@RequestBody UUID paymentId);
}
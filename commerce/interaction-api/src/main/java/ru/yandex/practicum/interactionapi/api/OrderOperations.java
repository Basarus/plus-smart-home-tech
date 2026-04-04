package ru.yandex.practicum.interactionapi.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.interactionapi.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/order")
public interface OrderOperations {

    @PutMapping
    OrderDto createNewOrder(@RequestBody CreateNewOrderRequest request);

    @PostMapping("/return")
    OrderDto productReturn(@RequestBody UUID orderId);

    @PostMapping("/payment")
    OrderDto payment(@RequestBody UUID orderId);

    @PostMapping("/payment/success")
    OrderDto paymentSuccess(@RequestBody UUID orderId);

    @PostMapping("/payment/failed")
    OrderDto paymentFailed(@RequestBody UUID orderId);

    @PostMapping("/delivery")
    OrderDto delivery(@RequestBody UUID orderId);

    @PostMapping("/delivery/failed")
    OrderDto deliveryFailed(@RequestBody UUID orderId);

    @PostMapping("/assembly")
    OrderDto assembly(@RequestBody UUID orderId);

    @PostMapping("/assembly/failed")
    OrderDto assemblyFailed(@RequestBody UUID orderId);

    @PostMapping("/delivery/cost")
    BigDecimal calculateDeliveryCost(@RequestBody UUID orderId);

    @PostMapping("/total/cost")
    BigDecimal calculateTotalCost(@RequestBody UUID orderId);

    @GetMapping
    List<OrderDto> getUserOrders(@RequestParam("username") String username);

    @GetMapping("/{orderId}")
    OrderDto getOrder(@PathVariable UUID orderId);
}
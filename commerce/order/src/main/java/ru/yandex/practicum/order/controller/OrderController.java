package ru.yandex.practicum.order.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interactionapi.api.OrderOperations;
import ru.yandex.practicum.interactionapi.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.order.service.OrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
public class OrderController implements OrderOperations {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        return orderService.createNewOrder(request);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        return orderService.payment(orderId);
    }

    @Override
    public OrderDto paymentSuccess(UUID orderId) {
        return orderService.paymentSuccess(orderId);
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        return orderService.paymentFailed(orderId);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        return orderService.delivery(orderId);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        return orderService.deliveryFailed(orderId);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        return orderService.assembly(orderId);
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        return orderService.assemblyFailed(orderId);
    }

    @Override
    public OrderDto productReturn(UUID orderId) {
        return orderService.productReturn(orderId);
    }

    @Override
    public List<OrderDto> getUserOrders(String username) {
        return orderService.getUserOrders(username);
    }

    @Override
    public OrderDto getOrder(UUID orderId) {
        return orderService.getOrder(orderId);
    }

    @Override
    public BigDecimal calculateDeliveryCost(UUID orderId) {
        return orderService.calculateDeliveryCost(orderId);
    }

    @Override
    public BigDecimal calculateTotalCost(UUID orderId) {
        return orderService.calculateTotalCost(orderId);
    }
}
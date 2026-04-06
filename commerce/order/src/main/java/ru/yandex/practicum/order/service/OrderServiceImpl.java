package ru.yandex.practicum.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.interactionapi.dto.delivery.DeliveryDto;
import ru.yandex.practicum.interactionapi.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.interactionapi.dto.payment.PaymentDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.AddressDto;
import ru.yandex.practicum.interactionapi.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.interactionapi.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.interactionapi.enums.DeliveryState;
import ru.yandex.practicum.interactionapi.enums.OrderState;
import ru.yandex.practicum.order.client.DeliveryClient;
import ru.yandex.practicum.order.client.PaymentClient;
import ru.yandex.practicum.order.client.ShoppingCartClient;
import ru.yandex.practicum.order.client.WarehouseClient;
import ru.yandex.practicum.order.exception.NoOrderFoundException;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.model.Order;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ShoppingCartClient shoppingCartClient;
    private final WarehouseClient warehouseClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ShoppingCartClient shoppingCartClient,
                            WarehouseClient warehouseClient,
                            PaymentClient paymentClient,
                            DeliveryClient deliveryClient,
                            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.shoppingCartClient = shoppingCartClient;
        this.warehouseClient = warehouseClient;
        this.paymentClient = paymentClient;
        this.deliveryClient = deliveryClient;
        this.orderMapper = orderMapper;
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        ShoppingCartDto shoppingCart = shoppingCartClient.getShoppingCart(request.username());

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setShoppingCartId(request.shoppingCartId());
        order.setUsername(request.username());
        order.setProducts(shoppingCart.products());
        order.setState(OrderState.NEW);

        orderMapper.mapToAddress(order, request.toAddress());

        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        Order order = getOrderEntity(orderId);

        if (order.getDeliveryWeight() == null
                || order.getDeliveryVolume() == null
                || order.getFragile() == null) {
            BookedProductsDto booked = warehouseClient.assemblyProductsForOrder(
                    new AssemblyProductsForOrderRequest(order.getProducts(), order.getOrderId())
            );
            order.setDeliveryWeight(booked.deliveryWeight());
            order.setDeliveryVolume(booked.deliveryVolume());
            order.setFragile(booked.fragile());
            order.setState(OrderState.ASSEMBLED);
        }

        if (isBlank(order.getFromStreet())) {
            AddressDto warehouseAddress = warehouseClient.getAddress();
            orderMapper.mapFromAddress(order, warehouseAddress);
        }

        if (order.getDeliveryPrice() == null) {
            order.setDeliveryPrice(deliveryClient.deliveryCost(orderMapper.toDto(order)));
        }

        if (order.getDeliveryId() == null) {
            DeliveryDto delivery = deliveryClient.planDelivery(
                    new DeliveryDto(
                            null,
                            orderMapper.buildFromAddress(order),
                            orderMapper.buildToAddress(order),
                            order.getOrderId(),
                            DeliveryState.CREATED
                    )
            );
            order.setDeliveryId(delivery.deliveryId());
        }

        order.setProductPrice(paymentClient.productCost(orderMapper.toDto(order)));
        order.setTotalPrice(paymentClient.getTotalCost(orderMapper.toDto(order)));

        PaymentDto payment = paymentClient.payment(orderMapper.toDto(order));
        order.setPaymentId(payment.paymentId());
        order.setState(OrderState.ON_PAYMENT);

        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto paymentSuccess(UUID orderId) {
        Order order = getOrderEntity(orderId);
        order.setState(OrderState.PAID);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        Order order = getOrderEntity(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        Order order = getOrderEntity(orderId);
        order.setState(OrderState.DELIVERED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        Order order = getOrderEntity(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        Order order = getOrderEntity(orderId);
        order.setState(OrderState.ON_DELIVERY);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        Order order = getOrderEntity(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Override
    public OrderDto productReturn(UUID orderId) {
        Order order = getOrderEntity(orderId);
        warehouseClient.acceptReturn(order.getProducts());
        order.setState(OrderState.PRODUCT_RETURNED);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(String username) {
        return orderRepository.findAllByUsername(username)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrder(UUID orderId) {
        return orderMapper.toDto(getOrderEntity(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDeliveryCost(UUID orderId) {
        Order order = getOrderEntity(orderId);
        return deliveryClient.deliveryCost(orderMapper.toDto(order));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCost(UUID orderId) {
        Order order = getOrderEntity(orderId);
        return paymentClient.getTotalCost(orderMapper.toDto(order));
    }

    private Order getOrderEntity(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(orderId));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
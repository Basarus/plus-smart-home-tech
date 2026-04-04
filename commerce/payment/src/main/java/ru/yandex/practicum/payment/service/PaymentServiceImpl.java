package ru.yandex.practicum.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.interactionapi.dto.payment.PaymentDto;
import ru.yandex.practicum.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.interactionapi.enums.PaymentState;
import ru.yandex.practicum.payment.client.OrderClient;
import ru.yandex.practicum.payment.client.ShoppingStoreClient;
import ru.yandex.practicum.payment.model.Payment;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              ShoppingStoreClient shoppingStoreClient,
                              OrderClient orderClient) {
        this.paymentRepository = paymentRepository;
        this.shoppingStoreClient = shoppingStoreClient;
        this.orderClient = orderClient;
    }

    @Override
    public PaymentDto payment(OrderDto orderDto) {
        double productCost = productCost(orderDto);
        double feeTotal = productCost * 0.10;
        double deliveryTotal = safe(orderDto.deliveryPrice());
        double totalPayment = productCost + feeTotal + deliveryTotal;

        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setOrderId(orderDto.orderId());
        payment.setProductTotal(productCost);
        payment.setDeliveryTotal(deliveryTotal);
        payment.setFeeTotal(feeTotal);
        payment.setTotalPayment(totalPayment);
        payment.setState(PaymentState.PENDING);

        paymentRepository.save(payment);

        return toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalCost(OrderDto orderDto) {
        double products = productCost(orderDto);
        double fee = products * 0.10;
        return products + fee + safe(orderDto.deliveryPrice());
    }

    @Override
    @Transactional(readOnly = true)
    public Double productCost(OrderDto orderDto) {
        double total = 0.0;

        for (Map.Entry<UUID, Long> entry : orderDto.products().entrySet()) {
            ProductDto product = shoppingStoreClient.getProductById(entry.getKey());
            total += product.price() * entry.getValue();
        }

        return total;
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        Payment payment = getPayment(paymentId);
        payment.setState(PaymentState.SUCCESS);
        paymentRepository.save(payment);

        orderClient.paymentSuccess(payment.getOrderId());
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        Payment payment = getPayment(paymentId);
        payment.setState(PaymentState.FAILED);
        paymentRepository.save(payment);

        orderClient.paymentFailed(payment.getOrderId());
    }

    private Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Оплата не найдена: " + paymentId));
    }

    private PaymentDto toDto(Payment payment) {
        return new PaymentDto(
                payment.getPaymentId(),
                payment.getTotalPayment(),
                payment.getDeliveryTotal(),
                payment.getFeeTotal(),
                payment.getProductTotal(),
                payment.getOrderId(),
                payment.getState()
        );
    }

    private double safe(Double value) {
        return value == null ? 0.0 : value;
    }
}
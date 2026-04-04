package ru.yandex.practicum.delivery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.interactionapi.dto.payment.PaymentDto;
import ru.yandex.practicum.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.interactionapi.enums.PaymentState;
import ru.yandex.practicum.delivery.client.OrderClient;
import ru.yandex.practicum.delivery.client.ShoppingStoreClient;
import ru.yandex.practicum.delivery.model.Payment;
import ru.yandex.practicum.delivery.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

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
        BigDecimal productCost = productCost(orderDto);
        BigDecimal feeTotal = productCost.multiply(TAX_RATE);
        BigDecimal deliveryTotal = safe(orderDto.deliveryPrice());
        BigDecimal totalPayment = productCost.add(feeTotal).add(deliveryTotal);

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
    public BigDecimal getTotalCost(OrderDto orderDto) {
        BigDecimal products = productCost(orderDto);
        BigDecimal fee = products.multiply(TAX_RATE);
        return products.add(fee).add(safe(orderDto.deliveryPrice()));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal productCost(OrderDto orderDto) {
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<UUID, Long> entry : orderDto.products().entrySet()) {
            ProductDto product = shoppingStoreClient.getProductById(entry.getKey());

            BigDecimal itemTotal = BigDecimal.valueOf(product.price())
                    .multiply(BigDecimal.valueOf(entry.getValue()));

            total = total.add(itemTotal);
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

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
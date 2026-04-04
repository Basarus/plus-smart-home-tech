package ru.yandex.practicum.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ru.yandex.practicum.interactionapi.enums.PaymentState;

import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "product_total")
    private Double productTotal;

    @Column(name = "delivery_total")
    private Double deliveryTotal;

    @Column(name = "fee_total")
    private Double feeTotal;

    @Column(name = "total_payment")
    private Double totalPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private PaymentState state;

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public Double getProductTotal() {
        return productTotal;
    }

    public void setProductTotal(Double productTotal) {
        this.productTotal = productTotal;
    }

    public Double getDeliveryTotal() {
        return deliveryTotal;
    }

    public void setDeliveryTotal(Double deliveryTotal) {
        this.deliveryTotal = deliveryTotal;
    }

    public Double getFeeTotal() {
        return feeTotal;
    }

    public void setFeeTotal(Double feeTotal) {
        this.feeTotal = feeTotal;
    }

    public Double getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(Double totalPayment) {
        this.totalPayment = totalPayment;
    }

    public PaymentState getState() {
        return state;
    }

    public void setState(PaymentState state) {
        this.state = state;
    }
}
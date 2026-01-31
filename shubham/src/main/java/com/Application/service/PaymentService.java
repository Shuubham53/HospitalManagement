package com.Application.service;

import com.Application.dto.PaymentRequest;
import com.Application.dto.PaymentResponse;
import com.Application.dto.RefundRequest;
import com.Application.entity.Billing;
import com.Application.entity.type.PaymentMethod;
import com.Application.entity.type.PaymentStatus;
import com.Application.error.BillNotFoundException;
import com.Application.repository.BillingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BillingRepository billingRepository;
    private final BillingService billingService;

    @Value("${stripe.public.key}")
    private String publicKey;

    @Value("${stripe.default.currency:inr}")
    private String defaultCurrency;

    @Transactional
    public PaymentResponse createPaymentIntent(PaymentRequest request) {
        log.info("Creating payment intent for bill ID: {}", request.getBillId());

        // Get billing
        Billing billing = billingService.getBillingEntity(request.getBillId());

        // Validate billing status
        if(billing.getStatus() != PaymentStatus.UNPAID && billing.getStatus() != PaymentStatus.FAILED){
            throw new IllegalStateException("Cannot create payment intent. Bill status: " + billing.getStatus());
        }

        // Check if payment already initiated
        if(billing.getReferenceNumber() != null && billing.getStatus() == PaymentStatus.CREATED){
            throw new IllegalStateException("Payment already initiated for this bill");
        }

        try {
            // Use provided currency or default
            String currency = request.getCurrency() != null ? request.getCurrency() : defaultCurrency;

            // Create Stripe Payment Intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(billing.getAmount().multiply(new BigDecimal(100)).longValue()) // Convert to smallest unit
                    .setCurrency(currency.toLowerCase())
                    .putMetadata("billingId", billing.getId().toString())
                    .putMetadata("patientId", billing.getPatient().getId().toString())
                    .putMetadata("appointmentId", billing.getAppointment().getId().toString())
                    .setDescription("Medical bill payment for patient: " + billing.getPatient().getFirst_name()+ " "+billing.getPatient().getLast_name())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Update billing with Stripe reference
            billing.setReferenceNumber(paymentIntent.getId());
            billing.setStatus(PaymentStatus.CREATED);
            billing.setPaymentMethod(PaymentMethod.CARD);
            billingRepository.save(billing);

            log.info("Payment intent created successfully: {} for bill ID: {}",
                    paymentIntent.getId(), billing.getId());

            return PaymentResponse.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .publicKey(publicKey)
                    .billingId(billing.getId())
                    .status(paymentIntent.getStatus())
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error while creating payment intent: {}", e.getMessage());
            throw new IllegalStateException("Failed to create payment intent: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Billing confirmPayment(String paymentIntentId) {
        log.info("Confirming payment for payment intent: {}", paymentIntentId);

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            Billing billing = billingRepository.findByReferenceNumber(paymentIntentId)
                    .orElseThrow(() -> new BillNotFoundException(
                            "No billing found for payment intent: " + paymentIntentId));

            // Update billing based on payment status
            switch (paymentIntent.getStatus()) {
                case "succeeded":
                    billing.setStatus(PaymentStatus.PAID);
                    billing.setPaidAt(LocalDateTime.now());
                    log.info("Payment succeeded for bill ID: {}", billing.getId());
                    break;

                case "processing":
                    billing.setStatus(PaymentStatus.PENDING);
                    log.info("Payment is processing for bill ID: {}", billing.getId());
                    break;

                case "canceled":
                    billing.setStatus(PaymentStatus.CANCELLED);
                    billing.setReferenceNumber(null);
                    log.warn("Payment canceled for bill ID: {}", billing.getId());
                    break;

                case "requires_payment_method":
                case "requires_confirmation":
                case "requires_action":
                    billing.setStatus(PaymentStatus.PENDING);
                    log.info("Payment requires action for bill ID: {}", billing.getId());
                    break;

                default:
                    billing.setStatus(PaymentStatus.FAILED);
                    billing.setReferenceNumber(null);
                    log.error("Payment failed for bill ID: {}, Status: {}",
                            billing.getId(), paymentIntent.getStatus());
                    break;
            }

            return billingRepository.save(billing);

        } catch (StripeException e) {
            log.error("Stripe error while confirming payment: {}", e.getMessage());
            throw new IllegalStateException("Failed to confirm payment: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Billing refundPayment(RefundRequest request) {
        log.info("Processing refund for payment intent: {}", request.getPaymentIntentId());

        Billing billing = billingRepository.findByReferenceNumber(request.getPaymentIntentId())
                .orElseThrow(() -> new BillNotFoundException(
                        "No billing found for payment intent: " + request.getPaymentIntentId()));

        if (billing.getStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot refund bill with status: " + billing.getStatus());
        }

        try {
            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(request.getPaymentIntentId());

            // Add amount if partial refund
            if (request.getAmount() != null) {
                paramsBuilder.setAmount(
                        request.getAmount().multiply(new BigDecimal(100)).longValue()
                );
            }

            // Add reason if provided
            if (request.getReason() != null) {
                try {
                    paramsBuilder.setReason(RefundCreateParams.Reason.valueOf(
                            request.getReason().toUpperCase()
                    ));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid refund reason: {}, using default", request.getReason());
                }
            }

            Refund refund = Refund.create(paramsBuilder.build());

            if ("succeeded".equals(refund.getStatus())) {
                billing.setStatus(PaymentStatus.REFUNDED);
                billing.setPaidAt(null);
                log.info("Refund succeeded for bill ID: {}", billing.getId());
            } else if ("pending".equals(refund.getStatus())) {
                billing.setStatus(PaymentStatus.PENDING);
                log.info("Refund pending for bill ID: {}", billing.getId());
            }

            return billingRepository.save(billing);

        } catch (StripeException e) {
            log.error("Stripe error while processing refund: {}", e.getMessage());
            throw new IllegalStateException("Failed to process refund: " + e.getMessage(), e);
        }
    }

    public Billing getBillingByPaymentIntent(String paymentIntentId) {
        return billingRepository.findByReferenceNumber(paymentIntentId)
                .orElseThrow(() -> new BillNotFoundException(
                        "No billing found for payment intent: " + paymentIntentId));
    }
}
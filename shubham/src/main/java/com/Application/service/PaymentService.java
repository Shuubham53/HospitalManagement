package com.Application.service;

import com.Application.dto.PaymentRequest;
import com.Application.dto.PaymentResponse;
import com.Application.dto.RefundRequest;
import com.Application.entity.Billing;
import com.Application.entity.Patient;
import com.Application.entity.User;
import com.Application.entity.type.PaymentMethod;
import com.Application.entity.type.PaymentStatus;
import com.Application.entity.type.Role;
import com.Application.error.BillNotFoundException;
import com.Application.error.BusinessRuleViolationException;
import com.Application.error.PatientNotFoundException;
import com.Application.repository.BillingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        User currentUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser.getRole() != Role.PATIENT) {
            throw new BusinessRuleViolationException(
                    "Only patients can create payment"
            );
        }
        Patient patient = currentUser.getPatient();

        if (patient == null) {
            throw new PatientNotFoundException(
                    "Patient profile not found for current user"
            );
        }

        Long patientId = currentUser.getPatient().getId();

        Billing billing = billingService.getBillingEntity(request.getBillId());
        Long billingPatientId = billing.getPatient().getId();

        if(!patientId.equals(billingPatientId)){
            throw new BusinessRuleViolationException("Unauthorize ...you dont have access to create payment");
        }

        if(billing.getStatus() != PaymentStatus.UNPAID && billing.getStatus() != PaymentStatus.FAILED){
            throw new IllegalStateException("Cannot create payment intent. Bill status: " + billing.getStatus());
        }


        if (billing.getAmount() == null ||
                billing.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException(
                    "Payment amount must be greater than zero"
            );
        }

        try {
            String currency = defaultCurrency;

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
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = (User) authentication.getPrincipal();

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            String metadataBillingId = paymentIntent.getMetadata().get("billingId");
            String metadataPatientId = paymentIntent.getMetadata().get("patientId");

            if (metadataBillingId == null || metadataPatientId == null) {
                throw new BusinessRuleViolationException(
                        "Payment intent metadata is invalid"
                );
            }

            Long billingId = Long.valueOf(metadataBillingId);
            Long patientId = Long.valueOf(metadataPatientId);

            Billing billing = billingRepository.findByReferenceNumber(paymentIntentId)
                    .orElseThrow(() -> new BillNotFoundException(
                            "No billing found for payment intent: " + paymentIntentId));

            if (!billingId.equals(billing.getId())) {
                throw new BusinessRuleViolationException(
                        "Payment intent does not belong to this billing"
                );
            }

            if (!patientId.equals(billing.getPatient().getId())) {
                throw new BusinessRuleViolationException(
                        "Payment intent does not belong to this patient"
                );
            }
            if (currentUser.getRole() != Role.PATIENT
                    || currentUser.getPatient() == null
                    || !currentUser.getPatient().getId().equals(billing.getPatient().getId())) {

                throw new BusinessRuleViolationException(
                        "Unauthorized: you cannot confirm this payment"
                );
            }

            BigDecimal paidAmount = BigDecimal.valueOf(paymentIntent.getAmount())
                    .divide(BigDecimal.valueOf(100));

            if (billing.getAmount().compareTo(paidAmount) != 0) {
                throw new BusinessRuleViolationException(
                        "Payment amount does not match billing amount"
                );
            }
            if(!defaultCurrency.equalsIgnoreCase(paymentIntent.getCurrency())){
                throw new BusinessRuleViolationException( "Payment currency does not match billing currency");
            }

            if(billing.getStatus() == PaymentStatus.PAID || billing.getStatus() == PaymentStatus.REFUNDED){
                return billing;
            }
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
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        User currentUser = (User) authentication.getPrincipal();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new BusinessRuleViolationException(
                    "Only admin can process refunds"
            );
        }

        Billing billing = billingRepository.findByReferenceNumber(request.getPaymentIntentId())
                .orElseThrow(() -> new BillNotFoundException(
                        "No billing found for payment intent: " + request.getPaymentIntentId()));

        if (billing.getStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot refund bill with status: " + billing.getStatus());
        }


        try {
            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(request.getPaymentIntentId());


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
                log.info("Refund succeeded for bill ID: {}", billing.getId());
            } else if ("pending".equals(refund.getStatus())) {
                log.info("Refund is still pending for bill ID: {}", billing.getId());

                throw new BusinessRuleViolationException(
                        "Refund is still being processed"
                );
            }

            return billingRepository.save(billing);

        } catch (StripeException e) {
            log.error("Stripe error while processing refund: {}", e.getMessage());
            throw new BusinessRuleViolationException("Failed to process refund: " + e.getMessage());
        }
    }

    @Transactional
    public Billing processWebhookPayment(String paymentIntentId) {
        log.info("Processing Stripe webhook for payment intent: {}", paymentIntentId);

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            String metadataBillingId = paymentIntent.getMetadata().get("billingId");
            String metadataPatientId = paymentIntent.getMetadata().get("patientId");

            if (metadataBillingId == null || metadataPatientId == null) {
                throw new BusinessRuleViolationException(
                        "Payment intent metadata is invalid"
                );
            }

            Long billingId = Long.valueOf(metadataBillingId);
            Long patientId = Long.valueOf(metadataPatientId);

            Billing billing = billingRepository.findByReferenceNumber(paymentIntentId)
                    .orElseThrow(() -> new BillNotFoundException(
                            "No billing found for payment intent: " + paymentIntentId
                    ));

            if (!billingId.equals(billing.getId())) {
                throw new BusinessRuleViolationException(
                        "Payment intent does not belong to this billing"
                );
            }

            if (!patientId.equals(billing.getPatient().getId())) {
                throw new BusinessRuleViolationException(
                        "Payment intent does not belong to this patient"
                );
            }

            BigDecimal paidAmount = BigDecimal.valueOf(paymentIntent.getAmount())
                    .divide(BigDecimal.valueOf(100));

            if (billing.getAmount().compareTo(paidAmount) != 0) {
                throw new BusinessRuleViolationException(
                        "Payment amount does not match billing amount"
                );
            }

            if (!defaultCurrency.equalsIgnoreCase(paymentIntent.getCurrency())) {
                throw new BusinessRuleViolationException(
                        "Payment currency does not match billing currency"
                );
            }

            // Idempotency: webhook can be delivered more than once
            if (billing.getStatus() == PaymentStatus.PAID ||
                    billing.getStatus() == PaymentStatus.REFUNDED) {
                return billing;
            }

            switch (paymentIntent.getStatus()) {

                case "succeeded":
                    billing.setStatus(PaymentStatus.PAID);
                    billing.setPaidAt(LocalDateTime.now());
                    log.info(
                            "Webhook: payment succeeded for bill ID: {}",
                            billing.getId()
                    );
                    break;

                case "processing":
                    billing.setStatus(PaymentStatus.PENDING);
                    break;

                case "canceled":
                    billing.setStatus(PaymentStatus.CANCELLED);
                    billing.setReferenceNumber(null);
                    break;

                case "requires_payment_method":
                case "requires_confirmation":
                case "requires_action":
                    billing.setStatus(PaymentStatus.PENDING);
                    break;

                default:
                    billing.setStatus(PaymentStatus.FAILED);
                    billing.setReferenceNumber(null);
                    log.warn(
                            "Webhook: payment failed for bill ID {}, status {}",
                            billing.getId(),
                            paymentIntent.getStatus()
                    );
            }

            return billingRepository.save(billing);

        } catch (StripeException e) {
            log.error(
                    "Stripe error while processing webhook payment: {}",
                    e.getMessage()
            );
            throw new IllegalStateException(
                    "Failed to process webhook payment",
                    e
            );
        }
    }
    public Billing getBillingByPaymentIntent(String paymentIntentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentLoggeduser = (User) authentication.getPrincipal();
        Billing billing =  billingRepository.findByReferenceNumber(paymentIntentId)
                .orElseThrow(() -> new BillNotFoundException(
                        "No billing found for payment intent: " + paymentIntentId));

        if(currentLoggeduser.getRole() == Role.ADMIN){
            return billing;
        }
        if(currentLoggeduser.getRole() != Role.PATIENT
                || currentLoggeduser.getPatient() == null
                || !currentLoggeduser.getPatient().getId().equals(billing.getPatient().getId())){
            throw new BusinessRuleViolationException(
                    "Unauthorized: you cannot access this billing"
            );
        }
        return billing;
    }
}
package com.Application.controller;


import com.Application.dto.PaymentConfirmRequest;
import com.Application.dto.PaymentRequest;
import com.Application.dto.PaymentResponse;
import com.Application.dto.RefundRequest;
import com.Application.entity.Billing;
import com.Application.service.BillingService;
import com.Application.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final BillingService billingService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/create-payment-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment intent creation request for bill ID: {}", request.getBillId());
        PaymentResponse response = paymentService.createPaymentIntent(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@Valid @RequestBody PaymentConfirmRequest request) {
        try {
            log.info("Received confirm request for payment intent: {}", request.getPaymentIntentId());
            Billing billing = paymentService.confirmPayment(request.getPaymentIntentId());
            return ResponseEntity.ok(billingService.mapToBillingResponse(billing));
        } catch (Exception e) {
            log.error("Error confirming payment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Confirm error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(@Valid @RequestBody RefundRequest request) {
        try {
            log.info("Received refund request for payment intent: {}", request.getPaymentIntentId());
            Billing billing = paymentService.refundPayment(request);
            return ResponseEntity.ok(billingService.mapToBillingResponse(billing));
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Refund error: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @GetMapping("/billing/{paymentIntentId}")
    public ResponseEntity<?> getBillingByPaymentIntent(@PathVariable String paymentIntentId) {
        try {
            Billing billing = paymentService.getBillingByPaymentIntent(paymentIntentId);
            return ResponseEntity.ok(billingService.mapToBillingResponse(billing));
        } catch (Exception e) {
            log.error("Error retrieving billing: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Billing not found: " + e.getMessage());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Received webhook from Stripe");

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        log.info("Webhook event type: {}", event.getType());

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                case "payment_intent.payment_failed":
                case "payment_intent.canceled": {
                    // raw event JSON, which is always reliable.
                    String paymentIntentId = extractIdFromEvent(event);

                    if (paymentIntentId == null) {
                        log.error("Could not extract PaymentIntent ID from webhook event: {}", event.getId());
                        break;
                    }

                    paymentService.processWebhookPayment(paymentIntentId);
                    log.info("{} webhook processed: {}", event.getType(), paymentIntentId);
                    break;
                }

                case "charge.refunded":
                    log.info("Refund webhook received");
                    break;

                default:
                    log.info("Unhandled webhook event type: {}", event.getType());
            }

            return ResponseEntity.ok("Webhook received");

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook processing error");
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String extractIdFromEvent(Event event) {
        try {
            String rawJson = event.getDataObjectDeserializer().getRawJson();
            JsonNode node = objectMapper.readTree(rawJson);
            if (node.has("id")) {
                return node.get("id").asText();
            }
        } catch (Exception e) {
            log.error("Failed to extract ID from webhook raw JSON", e);
        }
        return null;
    }
}
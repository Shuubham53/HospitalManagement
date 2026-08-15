package com.Application.controller;


import com.Application.dto.PaymentConfirmRequest;
import com.Application.dto.PaymentRequest;
import com.Application.dto.PaymentResponse;
import com.Application.dto.RefundRequest;
import com.Application.entity.Billing;
import com.Application.service.BillingService;
import com.Application.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody PaymentRequest request) {
        try {
            log.info("Received payment intent creation request for bill ID: {}", request.getBillId());
            PaymentResponse response = paymentService.createPaymentIntent(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating payment intent: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Payment error: " + e.getMessage());
        }
    }

//    @PostMapping("/confirm")
//    public ResponseEntity<?> confirmPayment(@RequestBody PaymentConfirmRequest request) {
//        try {
//            log.info("Received payment confirmation request for payment intent: {}",
//                    request.getPaymentIntentId());
//            Billing billing = paymentService.confirmPayment(request.getPaymentIntentId());
//            return ResponseEntity.ok(billingService.mapToBillingResponse(billing));
//        } catch (Exception e) {
//            log.error("Error confirming payment: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("Payment confirmation error: " + e.getMessage());
//        }
//    }

    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(@RequestBody RefundRequest request) {
        try {
            log.info("Received refund request for payment intent: {}", request.getPaymentIntentId());
            Billing billing = paymentService.refundPayment(request);
            return ResponseEntity.ok(billingService.mapToBillingResponse(billing));
        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Refund error: " + e.getMessage());
        }
    }

    @GetMapping("/billing/{paymentIntentId}")
    public ResponseEntity<?> getBillingByPaymentIntent(@PathVariable String paymentIntentId) {
        try {
            Billing billing = paymentService.getBillingByPaymentIntent(paymentIntentId);
            return ResponseEntity.ok(billingService.mapToBillingResponse(billing));
        } catch (Exception e) {
            log.error("Error retrieving billing: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Billing not found: " + e.getMessage());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Received webhook from Stripe");

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            log.info("Webhook event type: {}", event.getType());

            // Handle different event types
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                            .getObject().orElse(null);
                    if (paymentIntent != null) {
                        paymentService.confirmPayment(paymentIntent.getId());
                        log.info("Payment succeeded webhook processed: {}", paymentIntent.getId());
                    }
                    break;

                case "payment_intent.payment_failed":
                    PaymentIntent failedIntent = (PaymentIntent) event.getDataObjectDeserializer()
                            .getObject().orElse(null);
                    if (failedIntent != null) {
                        paymentService.confirmPayment(failedIntent.getId());
                        log.warn("Payment failed webhook processed: {}", failedIntent.getId());
                    }
                    break;

                case "payment_intent.canceled":
                    PaymentIntent canceledIntent = (PaymentIntent) event.getDataObjectDeserializer()
                            .getObject().orElse(null);
                    if (canceledIntent != null) {
                        paymentService.confirmPayment(canceledIntent.getId());
                        log.info("Payment canceled webhook processed: {}", canceledIntent.getId());
                    }
                    break;

                case "charge.refunded":
                    log.info("Refund webhook received");
                    break;

                default:
                    log.info("Unhandled webhook event type: {}", event.getType());
            }

            return ResponseEntity.ok("Webhook received");

        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook processing error");
        }
    }
}

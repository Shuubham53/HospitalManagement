package com.Application.service;

import com.Application.dto.BillingResponse;
import com.Application.entity.Appointment;
import com.Application.entity.Billing;
import com.Application.entity.type.AppointmentStatus;
import com.Application.entity.type.AppointmentType;
import com.Application.entity.type.PaymentMethod;
import com.Application.entity.type.PaymentStatus;
import com.Application.error.BillNotFoundException;
import com.Application.repository.BillingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {
    private final BillingRepository billingRepository;

    public BigDecimal calculateAmount(Appointment appointment){
        long minutes = Duration.between(appointment.getStartTime(), appointment.getEndTime()).toMinutes();
        log.info("Duration of Appointment {}",minutes);
        int slotMinutes = 30;
        BigDecimal feePerSlot = new BigDecimal("500");

        long slots = (minutes + slotMinutes - 1) / slotMinutes;
        BigDecimal amount = feePerSlot.multiply(BigDecimal.valueOf(slots));
        if(appointment.getAppointmentType() == AppointmentType.EMERGENCY){
            amount = amount.add(BigDecimal.valueOf(300));
        }
        if(appointment.getAppointmentType() == AppointmentType.FOLLOW_UP){
            amount = amount.subtract(BigDecimal.valueOf(200));
        }
        log.info("Amount to pay {}",amount);
        return amount;
    }

    public Billing generateBill(Appointment appointment) {
        if(appointment.getStatus() != AppointmentStatus.COMPLETED){
            throw new IllegalArgumentException("Cannot Generate Bill because Appointment is not Completed");
        }

        // Check if bill already exists
        if(billingRepository.existsByAppointment(appointment)){
            throw new IllegalStateException("Bill already exists for this appointment");
        }

        Billing billing = Billing.builder()
                .billing_date(LocalDateTime.now())
                .amount(calculateAmount(appointment))
                .status(PaymentStatus.UNPAID)
                .paymentMethod(null)
                .referenceNumber(null)
                .appointment(appointment)
                .patient(appointment.getPatient())
                .paidAt(null)
                .build();

        Billing savedBilling = billingRepository.save(billing);
        log.info("Bill generated successfully for appointment ID: {}, Bill ID: {}",
                appointment.getId(), savedBilling.getId());

        return savedBilling;
    }

    public BillingResponse mapToBillingResponse(Billing billing){
        return BillingResponse.builder()
                .id(billing.getId())
                .billingDate(billing.getBilling_date())
                .amount(billing.getAmount())
                .patientId(billing.getPatient().getId())
                .paymentMethod(billing.getPaymentMethod())
                .referenceNumber(billing.getReferenceNumber())
                .status(billing.getStatus())
                .build();
    }

    public BillingResponse getBillById(Long id) {
        Billing billing = billingRepository.findById(id).orElseThrow(() ->
                new BillNotFoundException("Bill not found with id "+id));
        return mapToBillingResponse(billing);
    }

    public List<BillingResponse> getAllBills() {
        List<Billing> billings = billingRepository.findAll();
        return billings.stream()
                .map(this::mapToBillingResponse).toList();
    }

    public List<BillingResponse> getBillsByStatus(String status) {
        PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        List<Billing> billings = billingRepository.findByStatus(paymentStatus);
        return billings.stream().map(this::mapToBillingResponse).toList();
    }

    public List<BillingResponse> getBillsByPatientId(Long patientId) {
        List<Billing> billings = billingRepository.findByPatientId(patientId);
        return billings.stream().map(this::mapToBillingResponse).toList();
    }

    public BillingResponse getBillsByReferenceId(String referenceId) {
        Billing billings = billingRepository.findByReferenceNumber(referenceId).orElseThrow(() ->
                new BillNotFoundException("Bill not found with reference number "+referenceId));
        return mapToBillingResponse(billings);
    }

    // Renamed for cash payments only
    public BillingResponse payBillCash(Long billId) {
        Billing billing = billingRepository.findById(billId).orElseThrow(() ->
                new BillNotFoundException("Bill not found for payment"));

        if(billing.getStatus() != PaymentStatus.UNPAID){
            throw new IllegalStateException("Bill has been already paid or refunded");
        }

        if (billing.getReferenceNumber() != null) {
            throw new IllegalStateException("Payment already processed");
        }

        billing.setPaymentMethod(PaymentMethod.CASH);
        billing.setStatus(PaymentStatus.PAID);
        billing.setReferenceNumber("CASH-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        billing.setPaidAt(LocalDateTime.now());
        billingRepository.save(billing);

        log.info("Cash payment completed for bill ID: {}", billId);
        return mapToBillingResponse(billing);
    }

    public BillingResponse refundBill(Long billId) {
        Billing billing = billingRepository.findById(billId).orElseThrow(() ->
                new BillNotFoundException("Bill not found for payment"));

        if(billing.getStatus() != PaymentStatus.PAID){
            throw new IllegalStateException("Bill must be Paid for refund but status is "+billing.getStatus());
        }

        billing.setStatus(PaymentStatus.REFUNDED);
        billing.setPaidAt(null);
        billingRepository.save(billing);

        log.info("Bill refunded for bill ID: {}", billId);
        return mapToBillingResponse(billing);
    }

    // Helper method for PaymentService
    public Billing getBillingByAppointment(Appointment appointment) {
        return billingRepository.findByAppointment(appointment)
                .orElseThrow(() -> new BillNotFoundException("No billing found for appointment ID: " + appointment.getId()));
    }

    public Billing getBillingEntity(Long billId) {
        return billingRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException("Bill not found with id " + billId));
    }
}
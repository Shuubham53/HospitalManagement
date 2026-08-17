package com.Application.service;

import com.Application.dto.BillingResponse;
import com.Application.entity.Appointment;
import com.Application.entity.Billing;
import com.Application.entity.User;
import com.Application.entity.type.*;
import com.Application.error.BillNotFoundException;
import com.Application.error.BusinessRuleViolationException;
import com.Application.repository.BillingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
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
        BigDecimal amount = new BigDecimal("500");

        if (appointment.getAppointmentType() == AppointmentType.EMERGENCY) {
            amount = amount.add(new BigDecimal("300"));
        }

        if (appointment.getAppointmentType() == AppointmentType.FOLLOW_UP) {
            amount = amount.subtract(new BigDecimal("200"));
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
        validateBillingAccess(billing);
        return mapToBillingResponse(billing);
    }

    public List<BillingResponse> getAllBills() {

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new BusinessRuleViolationException("Unauthorized: only admin can access all bills");
        }
        List<Billing> billings = billingRepository.findAll();
        return billings.stream()
                .map(this::mapToBillingResponse).toList();
    }

    public List<BillingResponse> getBillsByStatus(String status) {

        PaymentStatus paymentStatus;
        try {
            paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationException(
                    "Invalid Payment status: " + status
            );
        }
        List<Billing> billings = billingRepository.findByStatus(paymentStatus);
        return billings.stream().map(this::mapToBillingResponse).toList();
    }

    public List<BillingResponse> getBillsByPatientId(Long patientId) {
        User currentUser =
                (User) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

        if (currentUser.getRole() != Role.ADMIN
                && !(currentUser.getRole() == Role.PATIENT
                && currentUser.getPatient() != null
                && currentUser.getPatient().getId().equals(patientId))) {

            throw new BusinessRuleViolationException(
                    "Unauthorized: you cannot access these bills"
            );
        }
        List<Billing> billings = billingRepository.findByPatientId(patientId);
        return billings.stream().map(this::mapToBillingResponse).toList();
    }

    public BillingResponse getBillsByReferenceId(String referenceId) {
        Billing billings = billingRepository.findByReferenceNumber(referenceId).orElseThrow(() ->
                new BillNotFoundException("Bill not found with reference number "+referenceId));
        validateBillingAccess(billings);
        return mapToBillingResponse(billings);
    }


    public BillingResponse payBillCash(Long billId) {
        Billing billing = billingRepository.findById(billId).orElseThrow(() ->
                new BillNotFoundException("Bill not found for payment"));

        validateBillingAccess(billing);
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




    // Helper method for PaymentService
    public Billing getBillingByAppointment(Appointment appointment) {
        return billingRepository.findByAppointment(appointment)
                .orElseThrow(() -> new BillNotFoundException("No billing found for appointment ID: " + appointment.getId()));
    }

    public Billing getBillingEntity(Long billId) {
        return billingRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException("Bill not found with id " + billId));
    }

    private void validateBillingAccess(Billing billing) {

        User currentUser = (User) SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal();

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        if (currentUser.getRole() == Role.PATIENT
                && currentUser.getPatient() != null
                && currentUser.getPatient().getId().equals(billing.getPatient().getId())) {
            return;
        }

        throw new BusinessRuleViolationException(
                "Unauthorized: you cannot access this billing"
        );
    }
}
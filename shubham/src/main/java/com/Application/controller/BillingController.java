package com.Application.controller;

import com.Application.dto.BillingResponse;
import com.Application.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @GetMapping("/billings/{id}")
    public ResponseEntity<BillingResponse> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getBillById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/billings")
    public ResponseEntity<List<BillingResponse>> getAllBills() {
        return ResponseEntity.ok(billingService.getAllBills());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/billings/status/{status}")
    public ResponseEntity<List<BillingResponse>> getBillsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(billingService.getBillsByStatus(status));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @GetMapping("/billings/patient/{patientId}")
    public ResponseEntity<List<BillingResponse>> getBillsByPatientId(@PathVariable Long patientId) {
        return ResponseEntity.ok(billingService.getBillsByPatientId(patientId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @GetMapping("/billings/reference/{referenceId}")
    public ResponseEntity<BillingResponse> getBillsByReferenceId(@PathVariable String referenceId) {
        return ResponseEntity.ok(billingService.getBillsByReferenceId(referenceId));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/billings/{billId}/pay/cash")
    public ResponseEntity<BillingResponse> payBillCash(@PathVariable Long billId) {
        return ResponseEntity.ok(billingService.payBillCash(billId));
    }
}
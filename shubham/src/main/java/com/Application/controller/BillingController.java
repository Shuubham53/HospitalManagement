package com.Application.controller;

import com.Application.dto.BillingRequest;
import com.Application.dto.BillingResponse;
import com.Application.entity.type.AppointmentStatus;
import com.Application.entity.type.PaymentMethod;
import com.Application.service.BillingService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

//    @PostMapping("/billings")
//    public ResponseEntity<BillingResponse> generateBill(@RequestBody BillingRequest request){
//        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.generateBill(request));
//    }

    @GetMapping("/billings/{id}")
    public ResponseEntity<BillingResponse> getBillById(@PathVariable Long id){
        return ResponseEntity.ok(billingService.getBillById(id));
    }

    @GetMapping("/billings")
    public ResponseEntity<List<BillingResponse>> getAllBills(){
        return ResponseEntity.ok(billingService.getAllBills());
    }

    @GetMapping("/billings/status/{status}")
    public ResponseEntity<List<BillingResponse>> getBillsByStatus(@PathVariable String status){

        return ResponseEntity.ok(billingService.getBillsByStatus(status));
    }

    @GetMapping("/billings/patient/{patientId}")
    public ResponseEntity<List<BillingResponse>> getBillsByPatientId(@PathVariable Long patientId){
        return ResponseEntity.ok(billingService.getBillsByPatientId(patientId));
    }

    @GetMapping("/billings/reference/{referenceId}")
    public ResponseEntity<BillingResponse> getBillsByReferenceId(@PathVariable String referenceId){
        return ResponseEntity.ok(billingService.getBillsByReferenceId(referenceId));
    }

    @PutMapping("/billings/{billId}/pay/{paymentMethod}")
    public ResponseEntity<BillingResponse> payBillCash(@PathVariable PaymentMethod paymentMethod, @PathVariable Long billId){
        return ResponseEntity.ok(billingService.payBillCash(billId));
    }




}

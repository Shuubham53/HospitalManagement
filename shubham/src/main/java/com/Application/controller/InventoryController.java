package com.Application.controller;

import com.Application.dto.InventoryRequest;
import com.Application.dto.InventoryResponse;
import com.Application.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/inventories")
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody InventoryRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createInventory(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inventories/{inventoryId}")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long inventoryId){
        return ResponseEntity.ok(inventoryService.getInventoryById(inventoryId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inventories")
    public ResponseEntity<List<InventoryResponse>> getAllInventories(){
        return ResponseEntity.ok(inventoryService.getAllInventories());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/inventories/{id}")
    public ResponseEntity<InventoryResponse> updateInventory(
           @Valid @PathVariable Long id,
            @RequestBody InventoryRequest request) {

        return ResponseEntity.ok(inventoryService.updateInventory(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inventories/low-stock")
    public ResponseEntity<List<InventoryResponse>> getLowStockInventory(){
        return ResponseEntity.ok(inventoryService.getLowStockInventory());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inventories/expired")
    public ResponseEntity<List<InventoryResponse>> getExpiredInventory() {
        return ResponseEntity.ok(inventoryService.getExpiredInventory());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inventories/out-of-stock")
    public ResponseEntity<List<InventoryResponse>> getOutOfStockInventory() {
        return ResponseEntity.ok(inventoryService.getOutOfStockInventory());
    }
}

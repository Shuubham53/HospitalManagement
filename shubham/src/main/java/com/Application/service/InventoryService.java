package com.Application.service;

import com.Application.dto.InventoryRequest;
import com.Application.dto.InventoryResponse;
import com.Application.entity.Inventory;
import com.Application.entity.type.InventoryStatus;
import com.Application.error.ResourceNotFoundException;
import com.Application.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    public InventoryResponse createInventory(InventoryRequest request) {

        Inventory inventory = Inventory.builder()
                .category(request.getCategory())
                .expirationDate(request.getExpirationDate())
                .itemName(request.getItemName())
                .quantity(request.getQuantity())
                .reorderLevel(request.getReorderLevel())
                .status(calculateStatus(request.getQuantity(),request.getReorderLevel(),request.getExpirationDate()))
                .build();

        inventoryRepository.save(inventory);
        return mapToInventoryResponse(inventory);
    }
    @Transactional
    public InventoryStatus calculateStatus(Integer quantity, Integer reorderLevel, LocalDate expirationTime){

        if  (expirationTime != null &&
                expirationTime.isBefore(LocalDate.now())) {
            return InventoryStatus.EXPIRED;
        }
        else if (quantity == 0){
            return InventoryStatus.OUT_OF_STOCK;
        }
        else if(quantity <= reorderLevel){
            return InventoryStatus.LOW_STOCK;
        }

        return InventoryStatus.AVAILABLE;
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void updateExpiredInventory(){
        List<Inventory> inventories =
                inventoryRepository.findByExpirationDateBeforeAndStatusIn(
                        LocalDate.now(),
                        List.of(InventoryStatus.LOW_STOCK,InventoryStatus.AVAILABLE)
                );
        for(Inventory inventory : inventories){
            inventory.setStatus(InventoryStatus.EXPIRED);
        }

        inventoryRepository.saveAll(inventories);

    }

    public InventoryResponse mapToInventoryResponse(Inventory inventory){
        return InventoryResponse.builder()
                .id(inventory.getId())
                .status(inventory.getStatus())
                .category(inventory.getCategory())
                .expirationDate(inventory.getExpirationDate())
                .itemName(inventory.getItemName())
                .quantity(inventory.getQuantity())
                .reorderLevel(inventory.getReorderLevel())
                .build();
    }

    public InventoryResponse getInventoryById(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow(() ->
                new ResourceNotFoundException("Inventory not found with id "+inventoryId));
        return mapToInventoryResponse(inventory);
    }

    public List<InventoryResponse> getAllInventories() {
        List<Inventory> inventories = inventoryRepository.findAll();
        return inventories.stream().map(this::mapToInventoryResponse).toList();
    }

    public InventoryResponse updateInventory(Long id, InventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Inventory not found with id "+id));

        inventory.setItemName(request.getItemName());
        inventory.setQuantity(request.getQuantity());
        inventory.setReorderLevel(request.getReorderLevel());
        inventory.setExpirationDate(request.getExpirationDate());

        inventory.setStatus(calculateStatus(request.getQuantity(),request.getReorderLevel(),request.getExpirationDate()));
        inventoryRepository.save(inventory);

        return mapToInventoryResponse(inventory);
    }

    public List<InventoryResponse> getLowStockInventory() {
        List<Inventory> inventories = inventoryRepository.findByStatus(InventoryStatus.LOW_STOCK);
        return inventories.stream().map(this::mapToInventoryResponse).toList();
    }

    public List<InventoryResponse> getExpiredInventory() {
        List<Inventory> inventories = inventoryRepository.findByStatus(InventoryStatus.EXPIRED);
        return inventories.stream().map(this::mapToInventoryResponse).toList();
    }

    public List<InventoryResponse> getOutOfStockInventory() {
        List<Inventory> inventories = inventoryRepository.findByStatus(InventoryStatus.OUT_OF_STOCK);
        return inventories.stream().map(this::mapToInventoryResponse).toList();
    }
}

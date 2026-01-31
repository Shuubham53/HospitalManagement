package com.Application.dto;

import com.Application.entity.type.InventoryCategory;
import com.Application.entity.type.InventoryStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class InventoryResponse {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;

    @Enumerated(EnumType.STRING)
    private InventoryCategory category;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    private Integer quantity;

    private Integer reorderLevel;

    private LocalDate expirationDate;
}

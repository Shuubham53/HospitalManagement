package com.Application.dto;

import com.Application.entity.type.InventoryCategory;
import com.Application.entity.type.InventoryStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
@Data
public class InventoryRequest {

    @NotBlank(message = "Item name is required")
    @Size(max = 100, message = "Item name cannot exceed 100 characters")
    private String itemName;

    @NotNull(message = "Inventory category is required")
    private InventoryCategory category;

    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Reorder level is required")
    @PositiveOrZero(message = "Reorder level cannot be negative")
    private Integer reorderLevel;

    @NotNull(message = "Expiration date is required")
    @FutureOrPresent(message = "Expiration date cannot be in the past")
    private LocalDate expirationDate;
}
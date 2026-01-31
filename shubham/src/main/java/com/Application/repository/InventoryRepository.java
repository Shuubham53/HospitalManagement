package com.Application.repository;

import com.Application.entity.Inventory;
import com.Application.entity.type.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    List<Inventory> findByStatus(InventoryStatus inventoryStatus);
}

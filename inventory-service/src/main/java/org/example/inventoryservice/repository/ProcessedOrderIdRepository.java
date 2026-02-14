package org.example.inventoryservice.repository;

import org.example.inventoryservice.model.ProcessedOrderId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOrderIdRepository extends JpaRepository<ProcessedOrderId, String> {
    
}

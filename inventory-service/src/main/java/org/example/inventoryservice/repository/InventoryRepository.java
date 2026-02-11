package org.example.inventoryservice.repository;

import jakarta.annotation.PostConstruct;
import org.example.inventoryservice.event.OrderPlacedEvent;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InventoryRepository {
    private final Map<String, Integer> inventory = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        inventory.put("Smartphone", 5);
        inventory.put("Tablet", 10);
        inventory.put("Desktop", 6);
    }

    public void deductStock(OrderPlacedEvent orderPlacedEvent) {
        Integer result = inventory.computeIfPresent(orderPlacedEvent.productName(), (name, quantity) -> {
            if (quantity < orderPlacedEvent.quantity()) {
                throw new RuntimeException("Quantity exceeded");
            }

            return quantity - orderPlacedEvent.quantity();
        });

        if (result == null) {
            throw new RuntimeException("Stock exceeded");
        }
    }
}
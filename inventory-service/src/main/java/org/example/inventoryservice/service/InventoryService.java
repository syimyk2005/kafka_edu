package org.example.inventoryservice.service;

import org.example.inventoryservice.event.InventoryRejectedEvent;
import org.example.inventoryservice.event.InventoryReservedEvent;
import org.example.inventoryservice.event.OrderPlacedEvent;
import org.example.inventoryservice.repository.InventoryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryService(InventoryRepository inventoryRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order-placed")
    public void reserveInventory(OrderPlacedEvent orderPlacedEvent) {
        try {
            inventoryRepository.deductStock(orderPlacedEvent);

            InventoryReservedEvent inventoryReservedEvent = new InventoryReservedEvent(
                orderPlacedEvent.orderId(),
                orderPlacedEvent.email()
            );

            kafkaTemplate.send("inventory-reserved", inventoryReservedEvent);
        }
        catch (Exception e) {
            InventoryRejectedEvent inventoryRejectedEvent = new InventoryRejectedEvent(
                orderPlacedEvent.orderId(),
                orderPlacedEvent.email()
            );

            kafkaTemplate.send("inventory-rejected", inventoryRejectedEvent);
        }
    }
}
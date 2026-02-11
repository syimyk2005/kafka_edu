package org.example.inventoryservice.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.inventoryservice.event.InventoryRejectedEvent;
import org.example.inventoryservice.event.InventoryReservedEvent;
import org.example.inventoryservice.event.OrderPlacedEvent;
import org.example.inventoryservice.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);


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

            kafkaTemplate.send("inventory-reserved", inventoryReservedEvent.orderID(),inventoryReservedEvent);
        }
        catch (Exception e) {
            InventoryRejectedEvent inventoryRejectedEvent = new InventoryRejectedEvent(
                orderPlacedEvent.orderId(),
                orderPlacedEvent.email()
            );

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send("inventory-rejected", inventoryRejectedEvent.orderID(), inventoryRejectedEvent);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.info("Error sending inventory rejected event in thread {}", Thread.currentThread().getName());
                }
                else {
                    logger.info("Success sending inventory rejected event in thread {}", Thread.currentThread().getName());
                }
            });

        }
    }
}
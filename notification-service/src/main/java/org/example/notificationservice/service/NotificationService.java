package org.example.notificationservice.service;

import org.example.notificationservice.event.InventoryRejectedEvent;
import org.example.notificationservice.event.InventoryReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @KafkaListener(topics = "inventory-reserved")
    public void sendNotificationIfReserved(InventoryReservedEvent inventoryReservedEvent) {
        logger.info("Received inventory reserved event: {}", inventoryReservedEvent);
    }

    @KafkaListener(topics = "inventory-rejected")
    public void sendNotificationIfRejected(InventoryRejectedEvent inventoryRejectedEvent) {
        logger.info("Received inventory rejected event: {}", inventoryRejectedEvent);
    }
}

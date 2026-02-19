package org.example.inventoryservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.inventoryservice.event.InventoryRejectedEvent;
import org.example.inventoryservice.event.InventoryReservedEvent;
import org.example.inventoryservice.event.OrderPlacedEvent;
import org.example.inventoryservice.model.OutboxEvent;
import org.example.inventoryservice.model.ProcessedOrderId;
import org.example.inventoryservice.repository.InventoryRepository;
import org.example.inventoryservice.repository.OutboxEventRepository;
import org.example.inventoryservice.repository.ProcessedOrderIdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class InventoryProcessor {
    private final static Logger logger = LoggerFactory.getLogger(InventoryProcessor.class);
    private final InventoryRepository inventoryRepository;
    private final ProcessedOrderIdRepository processedOrderIdRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InventoryProcessor(
        InventoryRepository inventoryRepository,
        ProcessedOrderIdRepository processedOrderIdRepository,
        OutboxEventRepository outboxEventRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.processedOrderIdRepository = processedOrderIdRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public void processOrder(OrderPlacedEvent orderPlacedEvent) {
        try {
            processedOrderIdRepository.save(new ProcessedOrderId(
                orderPlacedEvent.orderId()
            ));
        } catch (DataIntegrityViolationException e) {
            logger.info("Order {} already processed", orderPlacedEvent.orderId());
            return;
        }

        int count = inventoryRepository.deductStock(orderPlacedEvent.productName(), orderPlacedEvent.quantity());
        String topic = (count > 0) ? "inventory-reserved" : "inventory-rejected";
        Object event = (count > 0)
            ? new InventoryReservedEvent(orderPlacedEvent.orderId(), orderPlacedEvent.email())
            : new InventoryRejectedEvent(orderPlacedEvent.orderId(), orderPlacedEvent.email());
        String json;

        try {
            json = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Serialization failed for order: " + orderPlacedEvent.orderId(), e);
        }

        OutboxEvent outboxEvent = new OutboxEvent(
            orderPlacedEvent.orderId(),
            topic,
            json
        );

        outboxEventRepository.save(outboxEvent);
    }
}

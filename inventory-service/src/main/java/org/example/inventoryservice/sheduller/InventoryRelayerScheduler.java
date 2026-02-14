package org.example.inventoryservice.sheduller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.inventoryservice.event.InventoryRejectedEvent;
import org.example.inventoryservice.event.InventoryReservedEvent;
import org.example.inventoryservice.model.OutboxEvent;
import org.example.inventoryservice.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.awt.print.Pageable;

import java.util.List;

@Component
public class InventoryRelayerScheduler {
    private final Logger logger = LoggerFactory.getLogger(InventoryRelayerScheduler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryRelayerScheduler(
        OutboxEventRepository outboxEventRepository,
        KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void relayOutboxEvents() {
        List<OutboxEvent> outboxEvents = outboxEventRepository.findBySentFalseOrderByCreatedAtAsc((Pageable) PageRequest.of(0, 100));

        for (OutboxEvent outboxEvent : outboxEvents) {
            try {
                String topic = outboxEvent.getTopic();
                String payloadJson = outboxEvent.getPayload();
                Object payload = (topic.equals("inventory-reserved"))
                    ? objectMapper.readValue(payloadJson, InventoryReservedEvent.class)
                    : objectMapper.readValue(payloadJson, InventoryRejectedEvent.class);

                kafkaTemplate.send(outboxEvent.getTopic(), outboxEvent.getKey(), payload).get();
                outboxEvent.setSent(true);
                outboxEventRepository.save(outboxEvent);
            }
            catch (Exception e) {
                logger.error("Failed to relay outbox event: {}", outboxEvent.getKey(), e);
            }
        }
    }
}
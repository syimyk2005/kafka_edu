package org.example.orderservice.service;

import org.example.orderservice.dto.OrderRequest;
import org.example.orderservice.event.OrderPlacedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class OrderService {
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public OrderService(KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String placeOrder(OrderRequest orderRequest) {
        String orderId = UUID.randomUUID().toString();
        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(
            orderId,
            orderRequest.email(),
            orderRequest.productName(),
            orderRequest.quantity()
        );

        kafkaTemplate.send("order-placed", orderPlacedEvent);

        return orderId;
    }
}
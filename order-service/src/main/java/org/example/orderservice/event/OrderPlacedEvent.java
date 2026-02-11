package org.example.orderservice.event;

public record OrderPlacedEvent(String orderId, String email, String productName, Integer quantity) {}
package org.example.inventoryservice.event;

public record OrderPlacedEvent(String orderId, String email, String productName, Integer quantity) {}
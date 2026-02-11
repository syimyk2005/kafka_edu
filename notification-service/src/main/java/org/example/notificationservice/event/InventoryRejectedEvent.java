package org.example.notificationservice.event;

public record InventoryRejectedEvent(String orderID, String email) {}

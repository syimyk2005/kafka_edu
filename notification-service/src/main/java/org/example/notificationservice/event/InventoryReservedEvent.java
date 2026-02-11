package org.example.notificationservice.event;

public record InventoryReservedEvent(String orderID, String email) {}

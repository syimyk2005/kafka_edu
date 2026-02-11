package org.example.inventoryservice.event;

public record InventoryReservedEvent(String orderID, String email) {}

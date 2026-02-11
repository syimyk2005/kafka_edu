package org.example.inventoryservice.event;

public record InventoryRejectedEvent(String orderID, String email) {}

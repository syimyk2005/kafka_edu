package org.example.orderservice.model.dto;

public record OrderRequest(String email, String productName, Integer quantity) {}


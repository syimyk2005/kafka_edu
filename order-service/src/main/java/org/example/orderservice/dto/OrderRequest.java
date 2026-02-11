package org.example.orderservice.dto;

public record OrderRequest(String email, String productName, Integer quantity) {}

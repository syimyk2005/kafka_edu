package org.example.inventoryservice.repository;

import org.example.inventoryservice.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    List<OutboxEvent> findBySentFalseOrderByCreatedAtAsc(Pageable pageable);
}

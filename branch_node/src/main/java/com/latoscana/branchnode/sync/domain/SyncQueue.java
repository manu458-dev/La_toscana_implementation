package com.latoscana.branchnode.sync.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "sync_queue", schema = "sync")
@Getter
@Setter
public class SyncQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;

    @Column(nullable = false, length = 50)
    private String action;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(nullable = false, length = 50)
    private String status = "PENDING"; // PENDING, PROCESSED, FAILED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SyncQueue() {}

    public SyncQueue(String entityName, String entityId, String action, String payload) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.action = action;
        this.payload = payload;
    }
}

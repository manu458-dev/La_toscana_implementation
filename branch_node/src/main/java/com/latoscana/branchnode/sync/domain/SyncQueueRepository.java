package com.latoscana.branchnode.sync.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyncQueueRepository extends JpaRepository<SyncQueue, Long> {
    List<SyncQueue> findByStatusOrderByCreatedAtAsc(String status);
}

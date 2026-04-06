package com.latoscana.branchnode.financial.infrastructure;

import com.latoscana.branchnode.financial.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByIssuedAtBetween(LocalDateTime start, LocalDateTime end);
}

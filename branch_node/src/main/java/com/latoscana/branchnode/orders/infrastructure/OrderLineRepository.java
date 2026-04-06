package com.latoscana.branchnode.orders.infrastructure;

import com.latoscana.branchnode.orders.domain.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
}

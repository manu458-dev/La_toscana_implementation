package com.latoscana.branchnode.orders.infrastructure;

import com.latoscana.branchnode.orders.domain.Order;
import com.latoscana.branchnode.orders.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
}

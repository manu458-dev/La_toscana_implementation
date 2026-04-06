package com.latoscana.branchnode.financial.application;

import com.latoscana.branchnode.financial.domain.Ticket;
import com.latoscana.branchnode.financial.domain.events.SaleClosedEvent;
import com.latoscana.branchnode.financial.infrastructure.SaleRepository;
import com.latoscana.branchnode.orders.domain.Order;
import com.latoscana.branchnode.orders.domain.OrderStatus;
import com.latoscana.branchnode.orders.infrastructure.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaleApplicationService {

    private final SaleRepository saleRepository;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRED)
    public Ticket closeSale(Long orderId, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.OPEN) {
            throw new IllegalStateException("Order is not OPEN");
        }

        order.setStatus(OrderStatus.CLOSED);
        orderRepository.save(order);

        Ticket ticket = new Ticket();
        ticket.setOrder(order);
        ticket.setTotalAmount(order.getTotal());
        ticket.setPaymentMethod(paymentMethod);

        Ticket savedTicket = saleRepository.save(ticket);
        
        // Publish event for EDA (e.g., Inventory deduction)
        eventPublisher.publishEvent(new SaleClosedEvent(this, savedTicket));
        
        return savedTicket;
    }
}

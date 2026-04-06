package com.latoscana.branchnode.inventory.infrastructure.listeners;

import com.latoscana.branchnode.financial.domain.Ticket;
import com.latoscana.branchnode.financial.domain.events.SaleClosedEvent;
import com.latoscana.branchnode.inventory.application.InventoryApplicationService;
import com.latoscana.branchnode.orders.domain.Order;
import com.latoscana.branchnode.orders.domain.OrderLine;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaleClosedEventListener {

    private final InventoryApplicationService inventoryApplicationService;

    @EventListener
    public void handleSaleClosedEvent(SaleClosedEvent event) {
        Ticket ticket = event.getTicket();
        Order order = ticket.getOrder();
        
        // Deduct inventory for each order line
        for (OrderLine line : order.getLines()) {
            inventoryApplicationService.deductInventoryForProduct(line.getProductId(), line.getQuantity());
        }
    }
}

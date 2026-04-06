package com.latoscana.branchnode.orders.presentation;

import com.latoscana.branchnode.orders.application.OrderApplicationService;
import com.latoscana.branchnode.orders.domain.Order;
import com.latoscana.branchnode.orders.domain.OrderStatus;
import com.latoscana.branchnode.orders.infrastructure.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderPublicAPI {

    private final OrderApplicationService orderApplicationService;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<Order> createOrder() {
        return ResponseEntity.ok(orderApplicationService.createOrder());
    }

    @PostMapping("/{orderId}/lines")
    public ResponseEntity<Order> addLine(
            @PathVariable Long orderId,
            @RequestParam Long productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(orderApplicationService.addLine(orderId, productId, quantity));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderApplicationService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{orderId}/lines/{lineId}")
    public ResponseEntity<Order> removeLine(
            @PathVariable Long orderId,
            @PathVariable Long lineId) {
        return ResponseEntity.ok(orderApplicationService.removeLine(orderId, lineId));
    }

    @GetMapping("/open")
    public ResponseEntity<List<Order>> getOpenOrders() {
        return ResponseEntity.ok(orderRepository.findByStatus(OrderStatus.OPEN));
    }
}

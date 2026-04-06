package com.latoscana.branchnode.orders.application;

import com.latoscana.branchnode.catalog.domain.Product;
import com.latoscana.branchnode.catalog.infrastructure.ProductRepository;
import com.latoscana.branchnode.orders.domain.Order;
import com.latoscana.branchnode.orders.domain.OrderLine;
import com.latoscana.branchnode.orders.domain.OrderStatus;
import com.latoscana.branchnode.orders.infrastructure.OrderLineRepository;
import com.latoscana.branchnode.orders.infrastructure.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional
    public Order createOrder() {
        Order order = new Order();
        return orderRepository.save(order);
    }

    @Transactional
    public Order addLine(Long orderId, Long productId, int quantity) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        OrderLine line = new OrderLine();
        line.setProductId(product.getId());
        line.setProductName(product.getName());
        line.setQuantity(quantity);
        line.setUnitPrice(product.getPrice());
        line.calculateSubtotal();

        order.addLine(line);
        orderLineRepository.save(line);
        return orderRepository.save(order);
    }

    @Transactional
    public Order removeLine(Long orderId, Long lineId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        OrderLine lineToRemove = order.getLines().stream()
                .filter(l -> l.getId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Line not found within order"));

        order.removeLine(lineToRemove);
        return orderRepository.save(order);
    }
}

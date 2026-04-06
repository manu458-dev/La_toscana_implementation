package com.latoscana.branchnode.financial.application;

import com.latoscana.branchnode.financial.domain.Ticket;
import com.latoscana.branchnode.financial.infrastructure.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditApplicationService {

    private final SaleRepository saleRepository;

    public Map<String, Object> generateAuditReport(LocalDateTime start, LocalDateTime end) {
        List<Ticket> tickets = saleRepository.findByIssuedAtBetween(start, end);

        BigDecimal totalSum = tickets.stream()
                .map(Ticket::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byPaymentMethod = tickets.stream()
                .collect(Collectors.groupingBy(
                        Ticket::getPaymentMethod,
                        Collectors.mapping(Ticket::getTotalAmount, 
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return Map.of(
            "start", start,
            "end", end,
            "totalTickets", tickets.size(),
            "totalSum", totalSum,
            "byPaymentMethod", byPaymentMethod
        );
    }
}

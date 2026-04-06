package com.latoscana.branchnode.financial.presentation;

import com.latoscana.branchnode.financial.application.PdfReceiptGenerator;
import com.latoscana.branchnode.financial.application.SaleApplicationService;
import com.latoscana.branchnode.financial.domain.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cashier")
@RequiredArgsConstructor
public class CashierPublicAPI {

    private final SaleApplicationService saleApplicationService;
    private final PdfReceiptGenerator pdfReceiptGenerator;

    @PostMapping("/close-sale")
    public ResponseEntity<byte[]> closeSaleAndGenerateReceipt(
            @RequestParam Long orderId,
            @RequestParam String paymentMethod) {

        Ticket ticket = saleApplicationService.closeSale(orderId, paymentMethod);
        byte[] pdfBytes = pdfReceiptGenerator.generateTicketPdf(ticket);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"receipt_" + ticket.getId() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}

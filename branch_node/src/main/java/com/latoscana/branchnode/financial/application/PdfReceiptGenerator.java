package com.latoscana.branchnode.financial.application;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.latoscana.branchnode.financial.domain.Ticket;
import com.latoscana.branchnode.orders.domain.OrderLine;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfReceiptGenerator {

    public byte[] generateTicketPdf(Ticket ticket) {
        // Roll of 80mm thermal receipt is roughly 226 points wide
        Document document = new Document(new Rectangle(226, 800)); 
        document.setMargins(10, 10, 10, 10);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            Paragraph header = new Paragraph("LA TOSCANA", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Ticket #" + ticket.getId() + "\nFecha: " + ticket.getIssuedAt() + "\n\n", regularFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(subHeader);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 1f, 1.5f});

            table.addCell(createCell("Producto", boldFont));
            table.addCell(createCell("Cant", boldFont));
            table.addCell(createCell("Total", boldFont));
            table.addCell(createCell("----------------", regularFont));
            table.addCell(createCell("---", regularFont));
            table.addCell(createCell("---------", regularFont));

            for (OrderLine line : ticket.getOrder().getLines()) {
                table.addCell(createCell(line.getProductName(), regularFont));
                table.addCell(createCell(String.valueOf(line.getQuantity()), regularFont));
                table.addCell(createCell("$" + line.getSubtotal().toString(), regularFont));
            }
            document.add(table);

            Paragraph totalSpacer = new Paragraph("\n", regularFont);
            document.add(totalSpacer);

            Paragraph total = new Paragraph("Total: $" + ticket.getTotalAmount().toString() + "\nMétodo: " + ticket.getPaymentMethod(), boldFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            Paragraph footer = new Paragraph("\n¡Gracias por su compra!", regularFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private PdfPCell createCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }
}

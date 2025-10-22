package net.sphuta.tms.freelancer.util;


import net.sphuta.tms.freelancer.dto.InvoicePayload;
import net.sphuta.tms.freelancer.dto.Item;
import net.sphuta.tms.freelancer.entity.Invoice;
import net.sphuta.tms.freelancer.entity.InvoiceItemEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ===============================================================
 * InvoiceMapper
 * ===============================================================
 * Utility class to convert Invoice entity to InvoicePayload DTO.
 *
 * ✅ Handles nested InvoiceItem → Item mapping
 * ✅ Keeps conversion logic centralized for reusability
 * ===============================================================
 */
public class InvoiceMapper {

    /**
     * Convert Invoice entity to InvoicePayload DTO
     *
     * @param invoice Invoice entity fetched from database
     * @return InvoicePayload DTO used for PDF generation and email
     */
    public static InvoicePayload toPayload(Invoice invoice) {
        List<Item> items = invoice.getItems().stream()
                .map(InvoiceMapper::mapItem)
                .collect(Collectors.toList());

        return new InvoicePayload(
                invoice.getFromCompany(),
                invoice.getFromAddress(),
                invoice.getFromPhone(),
                invoice.getFromEmail(),
                invoice.getClientName(),
                invoice.getClientAddress(),
                invoice.getClientPhone(),
                invoice.getInvoiceNo(),
                invoice.getInvoiceDate(),
                invoice.getDueDate(),
                invoice.getBankName(),
                invoice.getAccountNo(),
                items,
                invoice.getEmailTo(),
                invoice.getEmailSubject(),
                invoice.getEmailBody()
        );
    }

    /**
     * Convert InvoiceItem entity to Item DTO
     *
     * @param invoiceItem single InvoiceItem entity
     * @return Item DTO for use in InvoicePayload
     */
    private static Item mapItem(InvoiceItemEntity invoiceItem) {
        return new Item(
                invoiceItem.getType(),
                invoiceItem.getDescription(),
                invoiceItem.getQty(),
                invoiceItem.getUnitPrice(),
                invoiceItem.getAmount()
        );
    }
}

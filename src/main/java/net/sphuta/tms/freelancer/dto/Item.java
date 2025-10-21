package net.sphuta.tms.freelancer.dto;

import java.math.BigDecimal;

/**
 * ==============================================================
 * 📦 Item
 * ==============================================================
 * Represents a single line item in an invoice.
 *
 * This record is part of {@link net.sphuta.tms.freelancer.dto.InvoicePayload}
 * and is used to describe one billed product or service, including
 * its quantity, unit price, and total amount.
 * ==============================================================
 */
public record Item(

        /**
         * 🏷️ Type of the item or category.
         * Example: "Service", "Product", "Consulting", etc.
         */
        String type,

        /**
         * 📄 Description of the item or service provided.
         * Example: "Backend API development for project X"
         */
        String description,

        /**
         * 🔢 Quantity of the item being billed.
         * Example: 10 (hours, units, etc.)
         */
        Integer qty,

        /**
         * 💰 Unit price for each quantity of the item.
         * Example: 1500.00 (per hour, per unit, etc.)
         */
        BigDecimal unitPrice,

        /**
         * 💵 Total amount for this item.
         * Usually calculated as: {@code qty × unitPrice}
         */
        BigDecimal amount
) {

    /**
     * ==============================================================
     * ⚙️ Compact Constructor
     * ==============================================================
     * A convenient constructor allowing creation of an Item without
     * explicitly providing the total `amount`.
     *
     * Automatically computes:
     * <pre>
     * amount = unitPrice × qty
     * </pre>
     *
     * Example:
     * <pre>
     * new Item("Service", "UI Design", 5, new BigDecimal("2000"))
     * </pre>
     * will automatically set:
     * <pre>
     * amount = 10000.00
     * </pre>
     *
     * @param type        The item category
     * @param description The item or service description
     * @param qty         The quantity
     * @param unitPrice   The price per unit
     */
    public Item(String type, String description, Integer qty, BigDecimal unitPrice) {
        this(type, description, qty, unitPrice,
                unitPrice.multiply(BigDecimal.valueOf(qty)));
    }
}

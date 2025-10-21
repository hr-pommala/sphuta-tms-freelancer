package net.sphuta.tms.freelancer.dto;

import java.util.List;

/**
 * ==============================================================
 * 📘 InvoicePayload
 * ==============================================================
 * Data Transfer Object (DTO) representing the full payload
 * for generating a PDF invoice.
 *
 * This record is used by {@link net.sphuta.tms.freelancer.controller.ReportController}
 * in the POST `/reports/invoice` endpoint.
 *
 * It contains all the necessary fields for:
 * - Company (issuer) information
 * - Client (receiver) information
 * - Invoice metadata
 * - List of line items to be included in the invoice
 * ==============================================================
 */
public record InvoicePayload(

        /**
         * 🏢 Name of the company issuing the invoice.
         * Example: "Sphuta Technologies Pvt. Ltd."
         */
        String fromCompany,

        /**
         * 📍 Address of the issuing company.
         * Example: "Hyderabad, Telangana, India"
         */
        String fromAddress,

        /**
         * ☎️ Phone number of the issuing company.
         * Example: "+91 9876543210"
         */
        String fromPhone,

        /**
         * 📧 Email address of the issuing company.
         * Example: "billing@sphuta.com"
         */
        String fromEmail,

        /**
         * 👤 Name of the client being billed.
         * Example: "John Doe"
         */
        String clientName,

        /**
         * 🏠 Address of the client being billed.
         * Example: "123 Market Street, San Francisco, CA"
         */
        String clientAddress,

        /**
         * ☎️ Client’s contact number.
         * Example: "+1 415 555 1234"
         */
        String clientPhone,

        /**
         * 🧾 Unique invoice number identifier.
         * Example: "INV-2025-001"
         */
        String invoiceNo,

        /**
         * 📅 Invoice issue date.
         * Format: "YYYY-MM-DD"
         * Example: "2025-10-21"
         */
        String invoiceDate,

        /**
         * ⏰ Payment due date.
         * Format: "YYYY-MM-DD"
         * Example: "2025-10-31"
         */
        String dueDate,
        /**
         * 💼 Payment terms and conditions.
         * Example: "Payment due within 30 days"
         */
        String bankName,
        /**
         * 🏦 Bank account number for payment.
         * Example: "1234567890"
         */
        String accountNo,

        /**
         * 📦 List of individual invoice items.
         * Each {@link Item} contains:
         * - description
         * - quantity
         * - unit price
         * - total amount
         */
        List<Item> items

) {}

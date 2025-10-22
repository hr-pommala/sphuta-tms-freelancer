package net.sphuta.tms.freelancer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * InvoicePayload
 * ---------------------------------------------------------------
 * DTO carrying both invoice details and optional email metadata.
 * This version fixes Boolean field issues and ensures proper
 * Jackson (JSON) deserialization with nullable values.
 * ---------------------------------------------------------------
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InvoicePayload(
        // Invoice details
        String fromCompany,
        String fromAddress,
        String fromPhone,
        String fromEmail,
        String clientName,
        String clientAddress,
        String clientPhone,
        String invoiceNo,
        String invoiceDate,
        String dueDate,
        String bankName,
        String accountNo,
        List<Item> items,

        //email metadata
        String emailTo,
        String emailSubject,
        String emailBody
) { }

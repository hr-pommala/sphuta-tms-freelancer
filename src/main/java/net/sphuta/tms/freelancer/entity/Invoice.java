package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Invoice details
    private String fromCompany;
    private String fromAddress;
    private String fromPhone;
    private String fromEmail;

    private String clientName;
    private String clientAddress;
    private String clientPhone;

    @Column(unique = true)
    private String invoiceNo;

    @Column(name = "invoice_date")
    private String invoiceDate;

    @Column(name = "due_date")
    private String dueDate;

    private String bankName;
    private String accountNo;

    // Email metadata
    private String emailTo;
    private String emailSubject;
    private String emailBody;

    @Column(nullable = false)
    private String status; // e.g., DRAFT, SENT

    // Items in the invoice
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<InvoiceItemEntity> items;

}

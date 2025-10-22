package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Fetch all invoices with given status
     * Example: "DRAFT" → invoices not yet sent
     */
    List<Invoice> findByStatus(String status);
}

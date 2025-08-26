// InvoiceRepository.java
package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository interface for managing {@link Invoice} entities.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Provides built-in CRUD operations via {@link JpaRepository}.</li>
 *   <li>Defines additional query methods for invoice-specific lookups.</li>
 *   <li>Delegates implementation to Spring Data JPA at runtime.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * - Autowired in services to perform persistence operations on {@link Invoice} entities.
 * - Eliminates the need for boilerplate DAO code.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    /**
     * Check if an invoice exists for the given client.
     *
     * @param clientId unique identifier of the client
     * @return {@code true} if at least one invoice exists for the client, otherwise {@code false}
     */
    boolean existsByClientId(UUID clientId);
}

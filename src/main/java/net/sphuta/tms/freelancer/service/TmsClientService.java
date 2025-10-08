package net.sphuta.tms.freelancer.service;

import net.sphuta.tms.freelancer.dto.TmsClientDto;
import org.springframework.data.domain.Page;

/**
 * ==========================================================
 * {@code TmsClientService}
 * ==========================================================
 *
 * <h2>Contract for managing {@link net.sphuta.tms.freelancer.entity.ClientEntity}.</h2>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *     <li>CRUD operations for client records</li>
 *     <li>Archive / Unarchive client records</li>
 *     <li>CSV export of client data</li>
 * </ul>
 */
public interface TmsClientService {

    /**
     * Retrieve a paginated list of clients filtered by active status.
     *
     * @param active true = only active, false = only archived
     * @param search free-text search keyword
     * @param page   page number (0-based)
     * @param size   number of records per page
     * @return a paginated {@link Page} of {@link TmsClientDto}
     */
    Page<TmsClientDto> getClientlist(boolean active, String search, int page, int size);

    /**
     * Retrieve a paginated list of all clients (active and archived).
     *
     * @param search free-text search keyword
     * @param page   page number (0-based)
     * @param size   number of records per page
     * @return a paginated {@link Page} of {@link TmsClientDto}
     */
    Page<TmsClientDto> listAll(String search, int page, int size);

    /**
     * Fetch a single client by ID.
     *
     * @param id client ID
     * @return client details as {@link TmsClientDto}
     * @throws net.sphuta.tms.freelancer.exception.TmsException if not found
     */
    TmsClientDto get(int id);

    /**
     * Create a new client.
     *
     * @param req request payload containing client details
     * @return created client as {@link TmsClientDto}
     */
    TmsClientDto create(TmsClientDto req);

    /**
     * Update an existing client.
     *
     * @param id  client ID
     * @param req request payload with updated details
     * @return updated client as {@link TmsClientDto}
     */
    TmsClientDto update(int id, TmsClientDto req);

    /**
     * Delete a client by ID.
     *
     * @param id client ID
     */
    void delete(int id);

    /**
     * Archive (soft deactivate) a client.
     *
     * @param id client ID
     * @return archived client as {@link TmsClientDto}
     */
    TmsClientDto archive(int id);

    /**
     * Unarchive (reactivate) a client.
     *
     * @param id client ID
     * @return unarchived client as {@link TmsClientDto}
     */
    TmsClientDto unarchive(int id);

    /**
     * Export client data to CSV.
     *
     * @param activeFilter "true", "false", or "all"
     * @param search       search keyword
     * @return CSV content as byte array
     */
    byte[] exportCsv(String activeFilter, String search);

}
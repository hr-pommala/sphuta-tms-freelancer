
package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserRepository
 * <p>
 * JPA Repository for performing CRUD operations on User entities.
 * Extends JpaRepository to leverage Spring Data JPA methods.
 */
public interface TmsUserRepository extends JpaRepository<UserEntity, Integer> {

    /**
     * Find a User by their email address.
     * <p>
     * Returns an Optional containing the User if found, or empty if not.
     *
     * @param email the email of the user to search for
     * @return Optional<User> containing the found user or empty
     */
    Optional<UserEntity> findByEmail(String email);
}

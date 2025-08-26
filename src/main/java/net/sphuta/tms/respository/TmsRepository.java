package net.sphuta.tms.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import net.sphuta.tms.entity.TmsEntity;

public interface TmsRepository extends JpaRepository<TmsEntity, Long> {
    // Custom repository methods for TMS can be added here
}


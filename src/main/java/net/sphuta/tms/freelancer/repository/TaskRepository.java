package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for TaskEntity.
 * Provides CRUD operations and custom queries.
 */
public interface TaskRepository extends JpaRepository<TaskEntity, Integer> {

    /**
     * Find tasks by project id.
     * @param projectId the project id
     * @return list of tasks associated with the given project id
     * @see TaskEntity
     */
    List<TaskEntity> findByProjectId(int projectId);
}

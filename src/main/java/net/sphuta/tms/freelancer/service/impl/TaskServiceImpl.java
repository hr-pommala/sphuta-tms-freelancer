package net.sphuta.tms.freelancer.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TaskDto;
import net.sphuta.tms.freelancer.entity.ProjectEntity;
import net.sphuta.tms.freelancer.entity.TaskEntity;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.repository.TaskRepository;
import net.sphuta.tms.freelancer.repository.TmsProjectRepository;
import net.sphuta.tms.freelancer.service.TaskService;
import net.sphuta.tms.freelancer.util.TaskMappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Task service implementation.
 * Handles CRUD operations for tasks.
 * Validates that associated projects exist.
 * Logs creation and deletion events.
 * All methods are transactional.
 * Uses TaskMappers for entity/DTO conversion.
 * Throws NotFoundException for missing entities.
 * Read-only transactions for retrieval methods.
 * Depends on TaskRepository and TmsProjectRepository.
 * Implements TaskService interface.
 * Thread-safe singleton service.
 * Uses Lombok for logging and boilerplate reduction.
 */
@Slf4j
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    /** Repositories for data access
     */
    @Autowired
    private TaskRepository taskRepository;

    /** Repository for project validation
     */
    @Autowired
    private TmsProjectRepository projectRepository;

    /** Create a new task
     * Validates that the associated project exists
     * Logs the creation event
     * @param dto TaskDto with task details
     * @return Created TaskDto with generated id
     * @throws NotFoundException if the project does not exist
     * @see TaskMappers
     */
    @Override
    public TaskDto create(TaskDto dto) {
        log.info("Attempting to create a new task for projectId={}", dto.projectId());
        log.debug("Task create request payload: {}", dto);

        // verify project exists
//        if (dto.projectId() == null || !projectRepository.existsById(dto.projectId())) {
        ProjectEntity project = projectRepository.findById(dto.projectId())
                .orElseThrow(() ->{
            log.error("Project not found while creating task, projectId={}", dto.projectId());
            throw new NotFoundException("Project not found: " + dto.projectId());
        });

        TaskEntity e = TaskMappers.fromDto(dto, project);

        taskRepository.save(e);

        log.info("Task created successfully id={} projectId={}", e.getId(), e.getProjectId());
        return TaskMappers.toDto(e);
    }

    /** Update an existing task
     * Validates that the task exists
     * Optionally allows changing the associated project if provided
     * @param id Task id to update
     * @param dto TaskDto with updated details
     * @return Updated TaskDto
     * @throws NotFoundException if the task or new project does not exist
     * @see TaskMappers
     */
    @Override
    public TaskDto update(int id, TaskDto dto) {
        log.info("Attempting to update task id={}", id);
        log.debug("Task update request payload: {}", dto);

        var existing = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Task not found for update, id={}", id);
                    return new NotFoundException("Task not found: " + id);
                });

        if (dto.taskName() != null) existing.setTaskName(dto.taskName());
        existing.setDescription(dto.description());

        // optionally allow changing projectId if provided
        if (dto.projectId() != null) {
            if (!projectRepository.existsById(dto.projectId())) {
                log.error("Project not found while updating task id={}, projectId={}", id, dto.projectId());
                throw new NotFoundException("Project not found: " + dto.projectId());
            }
            existing.setProjectId(dto.projectId());
        }

        taskRepository.save(existing);

        log.info("Task updated successfully id={}", id);
        return TaskMappers.toDto(existing);
    }

    /** Get a task by id
     * @param id Task id
     * @return TaskDto with task details
     * @throws NotFoundException if the task does not exist
     * @see TaskMappers
     */
    @Override
    @Transactional(readOnly = true)
    public TaskDto get(int id) {
        log.info("Fetching task with id={}", id);

        TaskEntity e = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Task not found while fetching, id={}", id);
                    return new NotFoundException("Task not found: " + id);
                });

        log.debug("Fetched task entity: {}", e);
        return TaskMappers.toDto(e);
    }

    /** Get all tasks for a specific project
     * Validates that the project exists
     * @param projectId Project id
     * @return List of TaskDto for the project
     * @throws NotFoundException if the project does not exist
     * @see TaskMappers
     */
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getByProject(int projectId) {
        log.info("Fetching tasks for projectId={}", projectId);

        if (!projectRepository.existsById(projectId)) {
            log.error("Project not found while listing tasks, projectId={}", projectId);
            throw new NotFoundException("Project not found: " + projectId);
        }

        List<TaskEntity> list = taskRepository.findByProjectId(projectId);
        log.debug("Found {} tasks for projectId={}", list.size(), projectId);

        return TaskMappers.toDtoList(list);
    }

    /** Get all tasks
     * @return List of all TaskDto
     * @see TaskMappers
     * @see TaskRepository#findAll()
     * @see ProjectEntity
     */
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getAll() {
        log.info("Fetching all tasks");
        List<TaskEntity> entities = taskRepository.findAll();
        log.debug("Found {} tasks in total", entities.size());
        return TaskMappers.toDtoList(entities);
    }

    /** Delete a task by id
     * Validates that the task exists
     * Logs the deletion event
     * @param id Task id to delete
     * @throws NotFoundException if the task does not exist
     */
    @Override
    public void delete(int id) {
        log.warn("Attempting to delete task id={}", id);

        var e = taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Task not found for deletion, id={}", id);
                    return new NotFoundException("Task not found: " + id);
                });

        taskRepository.delete(e);

        log.info("Task deleted successfully id={}", id);
    }
}

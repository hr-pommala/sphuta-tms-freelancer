package net.sphuta.tms.freelancer.service;


import net.sphuta.tms.freelancer.dto.TaskDto;

import java.util.List;

/**
 * Service for Task CRUD operations.
 * Provides methods to create, update, retrieve, list, and delete tasks.
 * Acts as an intermediary between controllers and data access layers.
 * Ensures business logic and validation are applied to task operations.
 * Uses TaskDto for data transfer.
 * All methods throw appropriate exceptions for error handling.
 * Designed for use in a project management system.
 */
public interface TaskService {

    /** Create a new task.
     * @param dto TaskDto containing task details.
     * @return Created TaskDto with assigned ID.
     */
    TaskDto create(TaskDto dto);

    /** Update an existing task.
     * @param id ID of the task to update.
     * @param dto TaskDto containing updated task details.
     * @return Updated TaskDto.
     */
    TaskDto update(int id, TaskDto dto);

    /** Get a task by ID.
     * @param id ID of the task to retrieve.
     * @return TaskDto with the specified ID.
     */
    TaskDto get(int id);

    /** Get all tasks for a specific project.
     * @param projectId ID of the project whose tasks to retrieve.
     * @return List of TaskDto objects for the specified project.
     */
    List<TaskDto> getByProject(int projectId);

    /** Get all tasks.
     * @return List of all TaskDto objects.
     */
    List<TaskDto> getAll();

    /** Delete a task by ID.
     * @param id ID of the task to delete.
     */
    void delete(int id);
}

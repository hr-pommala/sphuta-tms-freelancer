package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.dto.TaskDto;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
import net.sphuta.tms.freelancer.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for tasks management.
 * Handles CRUD operations for tasks within projects.
 * Exposes RESTful endpoints for creating, updating, retrieving, listing, and deleting tasks.
 * Uses TaskService for business logic and data access.
 * All endpoints return standardized TmsApiResponse objects.
 * Validates input using Jakarta Bean Validation.
 * Annotated with OpenAPI for API documentation.
 * Base path for all endpoints is /api/v1.
 */
@RestController
@RequestMapping("/api/v1")
@Validated
@Slf4j
public class TaskController {

    /** Service for task operations. */
    @Autowired
    private TaskService taskService;

    /** Create a new task within a project.
     * @param projectId ID of the project to which the task belongs.
     * Must match the projectId in the request body if provided.
     * @param req TaskDto containing task details.
     * @return ResponseEntity with created TaskDto and HTTP status 201 (Created).
     * @throws IllegalArgumentException if projectId in path and body do not match.
     */
    @Operation(summary = "Create Task", description = "Create a new task within a specified project")
    @PostMapping("/projects/{projectId}/tasks")
    public TmsApiResponse<TaskDto> create(
            @PathVariable int projectId,
            @Valid @RequestBody TaskDto req) {

        log.info("Creating task for projectId: {}, request: {}", projectId, req);

        // ensure projectId in path syncs with payload (optional)
        var dto = new TaskDto(null, projectId, req.taskName(), req.description(), null, null);
        var created = taskService.create(dto);

        log.debug("Task created successfully: {}", created);

        return TmsApiResponse.created( TmsMessages.ENTITIES_CREATED, created);
    }

    /** Update an existing task.
     * @param id ID of the task to update.
     * @param req TaskDto containing updated task details.
     * @return ResponseEntity with updated TaskDto and HTTP status 200 (OK).
     */
    @Operation(summary = "Update Task", description = "Update an existing task by its ID")
    @PutMapping("/tasks/{id}")
    public TmsApiResponse<TaskDto> update(
            @PathVariable int id,
            @Valid @RequestBody TaskDto req) {

        log.info("Updating task with ID: {}, request: {}", id, req);

        var updated = taskService.update(id, req);

        log.debug("Task updated successfully: {}", updated);

        return TmsApiResponse.success(TmsMessages.ENTITIES_UPDATED, updated);
    }

    /** Get a task by ID.
     * @param id ID of the task to retrieve.
     * @return ResponseEntity with TaskDto and HTTP status 200 (OK).
     */
    @Operation(summary = "Get Task", description = "Retrieve a task by its ID")
    @GetMapping("/tasks/{id}")
    public TmsApiResponse<TaskDto> get(@PathVariable int id) {

        log.info("Fetching task with ID: {}", id);

        var data = taskService.get(id);

        log.debug("Fetched task: {}", data);

        return TmsApiResponse.success(TmsMessages.ENTITY_FETCHED, data);
    }

    /** List tasks by project ID.
     * @param projectId ID of the project whose tasks to list.
     * @return ResponseEntity with list of TaskDto and HTTP status 200 (OK).
     */
    @Operation(summary = "List tasks by project", description = "List all tasks associated with a specific project ID")
    @GetMapping("/projects/{projectId}/tasks")
    public TmsApiResponse<List<TaskDto>> listByProject(@PathVariable int projectId) {

        log.info("Listing tasks for projectId: {}", projectId);

        var list = taskService.getByProject(projectId);

        log.debug("Found {} tasks for projectId: {}", list.size(), projectId);

        return TmsApiResponse.success(TmsMessages.ENTITIES_FETCHED, list);
    }

    /** List all tasks.
     * @return ResponseEntity with list of all TaskDto and HTTP status 200 (OK).
     */
    @Operation(summary = "List all tasks", description = "Retrieve a list of all tasks across all projects")
    @GetMapping("/tasks")
    public TmsApiResponse<List<TaskDto>> listAll() {

        log.info("Listing all tasks");

        var list = taskService.getAll();

        log.debug("Found {} tasks in total", list.size());

        return TmsApiResponse.success(TmsMessages.ENTRIES_FETCHED, list);
    }

    /** Delete a task by ID.
     * @param id ID of the task to delete.
     * @return ResponseEntity with HTTP status 200 (OK) and deletion message.
     */
    @Operation(summary = "Delete Task", description = "Delete a task by its ID")
    @DeleteMapping("/tasks/{id}")
    public TmsApiResponse<Void> delete(@PathVariable int id) {

        log.warn("Deleting task with ID: {}", id);

        taskService.delete(id);

        log.info("Task deleted successfully with ID: {}", id);

        return TmsApiResponse.success(TmsMessages.ENTITY_DELETED, null);
    }
}


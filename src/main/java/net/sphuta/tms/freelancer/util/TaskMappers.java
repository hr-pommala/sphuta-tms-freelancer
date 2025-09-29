package net.sphuta.tms.freelancer.util;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TaskDto;
import net.sphuta.tms.freelancer.entity.ProjectEntity;
import net.sphuta.tms.freelancer.entity.TaskEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between TaskEntity and TaskDto.
 * Provides methods to convert single objects and lists.
 */

@Slf4j
public class TaskMappers {

    /** Private constructor to prevent instantiation */
    private TaskMappers() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static TaskDto toDto(TaskEntity e) {
        log.debug("Mapping TaskEntity to TaskDto: {}", e);
        TaskDto dto = new TaskDto(
                e.getId(),
                e.getProjectId(),
                e.getTaskName(),
                e.getDescription(),
                e.getCreatedDt(),
                e.getUpdatedDt()
        );
        log.trace("Mapped TaskDto: {}", dto);
        return dto;
    }

    public static TaskEntity fromDto(TaskDto d, ProjectEntity project) {
        log.debug("Mapping TaskDto to TaskEntity: {}", d);
        TaskEntity entity = TaskEntity.builder()
                .taskName(d.taskName())
                .description(d.description())
                .project(project)
                .build();
        log.trace("Mapped TaskEntity: {}", entity);
        return entity;
    }

    /** Converts a list of TaskEntity objects to a list of TaskDto objects
     * @param list List of TaskEntity objects
     * @return List of TaskDto objects
     */
    public static List<TaskDto> toDtoList(List<TaskEntity> list) {
        log.debug("Mapping list of TaskEntity (size={}) to TaskDto list",
                list != null ? list.size() : null);
        List<TaskDto> dtoList = list.stream()
                .map(TaskMappers::toDto)
                .collect(Collectors.toList());
        log.trace("Mapped TaskDto list (size={})", dtoList.size());
        return dtoList;
    }
}
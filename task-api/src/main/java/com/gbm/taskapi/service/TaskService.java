package com.gbm.taskapi.service;

import com.gbm.taskapi.model.Task;
import com.gbm.taskapi.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    // Cache individual task
    @Cacheable(value = "tasks", key = "#id", unless = "#result == null")
    public Optional<Task> findById(Long id) {
        log.info("DB QUERY: Finding task by ID: {} ", id);
        return taskRepository.findById(id);
    }

    // Cache task list by project (careful with pagination)
    @Cacheable(value = "projectTasks", key = "#projectId", unless = "#result.isEmpty()")
    public List<Task> findByProjectId(Long projectId) {
        log.info("DB QUERY: Finding tasks for project: {}", projectId);
        return taskRepository.findByProjectIdWithAssignee(projectId);
    }

    // Paginated tasks (don't cache - different page = different result)
    public Page<Task> findByProjectIdPaginated(Long projectId, Pageable pageable) {
        return taskRepository.findByProjectId(projectId, pageable);
    }

    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "tasks", key = "#task.id"),
                @CacheEvict(value = "projectTasks", key = "#task.project.id")
            })
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "tasks", key = "#task.id"),
                @CacheEvict(value = "projectTasks", key = "#task.project.id")
            })
    public Task updateTask(Task task) {
        log.info("CACHE EVICT: Updating task: {}", task.getId());
        return taskRepository.save(task);
    }

    @Transactional
    @Caching(
            evict = {@CacheEvict(value = "tasks", key = "#id"), @CacheEvict(value = "projectTasks", allEntries = true)})
    public void deleteTask(Long id) {
        log.info("CACHE EVICT: Deleting task: {}", id);
        taskRepository.deleteById(id);
    }
}

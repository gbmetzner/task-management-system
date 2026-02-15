package com.gbm.taskapi.service;

import com.gbm.taskapi.dto.request.CreateTaskRequest;
import com.gbm.taskapi.dto.request.UpdateTaskRequest;
import com.gbm.taskapi.dto.service.TaskResult;
import com.gbm.taskapi.model.Task;
import com.gbm.taskapi.model.TaskPriority;
import com.gbm.taskapi.model.TaskStatus;
import com.gbm.taskapi.model.User;
import com.gbm.taskapi.repository.ProjectRepository;
import com.gbm.taskapi.repository.TaskRepository;
import com.gbm.taskapi.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "tasks", key = "#id", unless = "#result == null")
    public Optional<TaskResult> findById(Long id) {
        log.info("DB QUERY: Finding task by ID: {} ", id);
        return taskRepository.findById(id).map(this::toTaskResult);
    }

    @Cacheable(value = "projectTasks", key = "#projectId", unless = "#result.isEmpty()")
    public List<TaskResult> findByProjectId(Long projectId) {
        log.info("DB QUERY: Finding tasks for project: {}", projectId);
        return taskRepository.findByProjectIdWithAssignee(projectId).stream()
                .map(this::toTaskResult)
                .toList();
    }

    public Page<TaskResult> findByProjectIdPaginated(Long projectId, Pageable pageable) {
        return taskRepository.findByProjectId(projectId, pageable).map(this::toTaskResult);
    }

    @Transactional
    @Caching(evict = {@CacheEvict(value = "projectTasks", key = "#result.project.id")})
    public TaskResult createTask(CreateTaskRequest request) {
        var project = projectRepository.findById(request.projectId()).orElseThrow();
        var reporter = userRepository.findById(getAuthenticatedUserId()).orElseThrow();

        var task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setProject(project);
        task.setReporter(reporter);
        task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        task.setDueDate(request.dueDate());

        if (request.assigneeId() != null) {
            var assignee = userRepository.findById(request.assigneeId()).orElseThrow();
            task.setAssignee(assignee);
        }

        return toTaskResult(taskRepository.save(task));
    }

    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "tasks", key = "#id"),
                @CacheEvict(value = "projectTasks", key = "#result.project.id")
            })
    public TaskResult updateTask(Long id, UpdateTaskRequest request) {
        var task = taskRepository.findById(id).orElseThrow();
        log.info("CACHE EVICT: Updating task: {}", id);

        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
        if (request.assigneeId() != null) {
            var assignee = userRepository.findById(request.assigneeId()).orElseThrow();
            task.setAssignee(assignee);
        }

        return toTaskResult(taskRepository.save(task));
    }

    @Transactional
    @Caching(
            evict = {@CacheEvict(value = "tasks", key = "#id"), @CacheEvict(value = "projectTasks", allEntries = true)})
    public void deleteTask(Long id) {
        log.info("CACHE EVICT: Deleting task: {}", id);
        taskRepository.deleteById(id);
    }

    public Page<TaskResult> searchTasks(
            String keyword, TaskStatus status, TaskPriority priority, Long assigneeId, Pageable pageable) {
        return taskRepository
                .searchTasks(keyword, status, priority, assigneeId, pageable)
                .map(this::toTaskResult);
    }

    private Long getAuthenticatedUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private TaskResult toTaskResult(Task task) {
        var project = task.getProject();
        var assignee = task.getAssignee();

        return new TaskResult(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                new TaskResult.ProjectInfo(project.getId(), project.getName(), project.getDescription()),
                assignee != null ? toUserInfo(assignee) : null,
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getComments() != null ? task.getComments().size() : 0);
    }

    private TaskResult.UserInfo toUserInfo(User user) {
        return new TaskResult.UserInfo(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                (user.getFirstName() != null ? user.getFirstName() : "")
                        + " "
                        + (user.getLastName() != null ? user.getLastName() : ""));
    }
}

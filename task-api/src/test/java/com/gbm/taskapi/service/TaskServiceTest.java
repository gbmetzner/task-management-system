package com.gbm.taskapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gbm.taskapi.TestContainerSupport;
import com.gbm.taskapi.dto.request.CreateTaskRequest;
import com.gbm.taskapi.dto.request.UpdateTaskRequest;
import com.gbm.taskapi.dto.service.TaskResult;
import com.gbm.taskapi.model.*;
import com.gbm.taskapi.repository.ProjectRepository;
import com.gbm.taskapi.repository.TaskRepository;
import com.gbm.taskapi.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

// @Transactional keeps the Hibernate session open during cache serialization.
// Without it, Redis/Jackson fails on lazy collections (comments, assignee.assignedTasks, etc.).
// Consider caching DTOs instead of entities (like UserService does) to avoid this in production.
@SpringBootTest
@Transactional
class TaskServiceTest extends TestContainerSupport {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private User reporter;
    private Project project;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache("tasks")).clear();
        Objects.requireNonNull(cacheManager.getCache("projectTasks")).clear();
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        reporter = new User();
        reporter.setEmail("reporter@test.com");
        reporter.setPassword("password");
        reporter.setRole(Role.USER);
        reporter = userRepository.save(reporter);

        var auth = new UsernamePasswordAuthenticationToken(
                reporter.getId(), null, Collections.singletonList(new SimpleGrantedAuthority(Role.USER.name())));
        SecurityContextHolder.getContext().setAuthentication(auth);

        project = new Project();
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setOwner(reporter);
        project.setStatus(ProjectStatus.ACTIVE);
        project = projectRepository.save(project);
    }

    private Task saveTask(String title) {
        Task task = new Task();
        task.setTitle(title);
        task.setProject(project);
        task.setReporter(reporter);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        return taskRepository.save(task);
    }

    @Test
    @DisplayName("Should find task by ID")
    void shouldFindTaskById() {
        Task saved = saveTask("Find Me");

        Optional<TaskResult> result = taskService.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("Find Me");
    }

    @Test
    @DisplayName("Should return empty when task not found")
    void shouldReturnEmptyWhenTaskNotFound() {
        Optional<TaskResult> result = taskService.findById(999999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should cache task by ID on repeated calls")
    void shouldCacheTaskById() {
        Task saved = saveTask("Cached Task");

        // First call — cache miss
        taskService.findById(saved.getId());

        // Second call — should hit cache
        taskService.findById(saved.getId());

        var cached = cacheManager.getCache("tasks").get(saved.getId());
        assertThat(cached).isNotNull();
    }

    @Test
    @DisplayName("Should find tasks by project ID")
    void shouldFindTasksByProjectId() {
        saveTask("Task 1");
        saveTask("Task 2");

        List<TaskResult> tasks = taskService.findByProjectId(project.getId());

        assertThat(tasks).hasSize(2);
    }

    @Test
    @DisplayName("Should cache tasks by project ID")
    void shouldCacheTasksByProjectId() {
        saveTask("Task 1");

        // First call — cache miss
        taskService.findByProjectId(project.getId());

        // Second call — should hit cache
        taskService.findByProjectId(project.getId());

        var cached = cacheManager.getCache("projectTasks").get(project.getId());
        assertThat(cached).isNotNull();
    }

    @Test
    @DisplayName("Should not cache empty project task list")
    void shouldNotCacheEmptyProjectTaskList() {
        taskService.findByProjectId(project.getId());

        var cached = cacheManager.getCache("projectTasks").get(project.getId());
        assertThat(cached).isNull();
    }

    @Test
    @DisplayName("Should create task successfully")
    void shouldCreateTask() {
        var request = new CreateTaskRequest("New Task", null, project.getId(), null, null, null, null);

        TaskResult created = taskService.createTask(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.title()).isEqualTo("New Task");
        assertThat(created.status()).isEqualTo(TaskStatus.TODO);
        assertThat(created.priority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(created.project().id()).isEqualTo(project.getId());
        assertThat(taskRepository.findById(created.id())).isPresent();
    }

    @Test
    @DisplayName("Should update task and evict caches")
    void shouldUpdateTaskAndEvictCaches() {
        Task saved = saveTask("Original");
        taskService.findById(saved.getId());

        var request = new UpdateTaskRequest("Updated", null, null, null, null, null);
        TaskResult updated = taskService.updateTask(saved.getId(), request);

        assertThat(updated.title()).isEqualTo("Updated");
        var cached = cacheManager.getCache("tasks").get(saved.getId());
        assertThat(cached).isNull();
    }

    @Test
    @DisplayName("Should delete task and evict caches")
    void shouldDeleteTaskAndEvictCaches() {
        Task saved = saveTask("To Delete");
        taskService.findById(saved.getId());

        taskService.deleteTask(saved.getId());

        var cached = cacheManager.getCache("tasks").get(saved.getId());
        assertThat(cached).isNull();
        assertThat(taskRepository.findById(saved.getId())).isEmpty();
    }
}

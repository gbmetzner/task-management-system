package com.gbm.taskapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gbm.taskapi.TestContainerSupport;
import com.gbm.taskapi.model.*;
import com.gbm.taskapi.repository.ProjectRepository;
import com.gbm.taskapi.repository.TaskRepository;
import com.gbm.taskapi.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
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

        Optional<Task> result = taskService.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Find Me");
    }

    @Test
    @DisplayName("Should return empty when task not found")
    void shouldReturnEmptyWhenTaskNotFound() {
        Optional<Task> result = taskService.findById(999999L);

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

        List<Task> tasks = taskService.findByProjectId(project.getId());

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
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setProject(project);
        newTask.setReporter(reporter);
        newTask.setStatus(TaskStatus.TODO);
        newTask.setPriority(TaskPriority.MEDIUM);

        Task created = taskService.createTask(newTask);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("New Task");
        assertThat(taskRepository.findById(created.getId())).isPresent();
    }

    @Test
    @DisplayName("Should update task and evict caches")
    void shouldUpdateTaskAndEvictCaches() {
        Task saved = saveTask("Original");
        taskService.findById(saved.getId());

        saved.setTitle("Updated");
        taskService.updateTask(saved);

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

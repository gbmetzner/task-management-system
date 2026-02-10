package com.gbm.taskapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gbm.taskapi.TestContainerSupport;
import com.gbm.taskapi.config.TaskAppConfig;
import com.gbm.taskapi.model.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TaskAppConfig.class)
class TaskRepositoryTest extends TestContainerSupport {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserRepository userRepository;

    private User reporter;
    private User assignee;
    private Project project;
    private Project otherProject;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        reporter = new User();
        reporter.setEmail("reporter@example.com");
        reporter.setPassword("password");
        reporter.setRole(Role.USER);
        reporter = userRepository.save(reporter);

        assignee = new User();
        assignee.setEmail("assignee@example.com");
        assignee.setPassword("password");
        assignee.setRole(Role.USER);
        assignee = userRepository.save(assignee);

        project = new Project();
        project.setName("Project A");
        project.setDescription("Description A");
        project.setOwner(reporter);
        project.setStatus(ProjectStatus.ACTIVE);
        project = projectRepository.save(project);

        otherProject = new Project();
        otherProject.setName("Project B");
        otherProject.setDescription("Description B");
        otherProject.setOwner(reporter);
        otherProject.setStatus(ProjectStatus.ACTIVE);
        otherProject = projectRepository.save(otherProject);
    }

    private Task createTask(String title, Project taskProject, User taskAssignee, TaskStatus status) {
        Task task = new Task();
        task.setTitle(title);
        task.setProject(taskProject);
        task.setReporter(reporter);
        task.setAssignee(taskAssignee);
        task.setStatus(status);
        task.setPriority(TaskPriority.MEDIUM);
        return taskRepository.save(task);
    }

    @Test
    @DisplayName("Should save and retrieve a task")
    void shouldSaveAndRetrieveTask() {
        Task task = createTask("Test Task", project, assignee, TaskStatus.TODO);

        assertThat(task.getId()).isNotNull();
        assertThat(task.getCreatedAt()).isNotNull();
        assertThat(taskRepository.findById(task.getId())).isPresent();
    }

    @Test
    @DisplayName("Should find tasks by project ID")
    void shouldFindByProjectId() {
        createTask("Task 1", project, assignee, TaskStatus.TODO);
        createTask("Task 2", project, null, TaskStatus.IN_PROGRESS);
        createTask("Task 3", otherProject, assignee, TaskStatus.TODO);

        List<Task> projectTasks = taskRepository.findByProjectId(project.getId());

        assertThat(projectTasks).hasSize(2);
        assertThat(projectTasks).allMatch(t -> t.getProject().getId().equals(project.getId()));
    }

    @Test
    @DisplayName("Should return empty list when project has no tasks")
    void shouldReturnEmptyWhenProjectHasNoTasks() {
        createTask("Task 1", project, assignee, TaskStatus.TODO);

        List<Task> tasks = taskRepository.findByProjectId(otherProject.getId());

        assertThat(tasks).isEmpty();
    }

    @Test
    @DisplayName("Should find tasks by assignee ID")
    void shouldFindByAssigneeId() {
        createTask("Assigned 1", project, assignee, TaskStatus.TODO);
        createTask("Assigned 2", otherProject, assignee, TaskStatus.DONE);
        createTask("Unassigned", project, null, TaskStatus.TODO);
        createTask("Reporter's task", project, reporter, TaskStatus.TODO);

        List<Task> assigneeTasks = taskRepository.findByAssigneeId(assignee.getId());

        assertThat(assigneeTasks).hasSize(2);
        assertThat(assigneeTasks).allMatch(t -> t.getAssignee().getId().equals(assignee.getId()));
    }

    @Test
    @DisplayName("Should find tasks by status")
    void shouldFindByStatus() {
        createTask("Todo 1", project, assignee, TaskStatus.TODO);
        createTask("Todo 2", otherProject, null, TaskStatus.TODO);
        createTask("In Progress", project, assignee, TaskStatus.IN_PROGRESS);
        createTask("Done", project, assignee, TaskStatus.DONE);

        List<Task> todoTasks = taskRepository.findByStatus(TaskStatus.TODO);
        List<Task> inProgressTasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS);
        List<Task> doneTasks = taskRepository.findByStatus(TaskStatus.DONE);

        assertThat(todoTasks).hasSize(2);
        assertThat(inProgressTasks).hasSize(1);
        assertThat(doneTasks).hasSize(1);
    }
}

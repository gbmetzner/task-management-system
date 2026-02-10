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
class ProjectRepositoryTest extends TestContainerSupport {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserRepository userRepository;

    private User owner;
    private User otherOwner;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User();
        owner.setEmail("owner@example.com");
        owner.setPassword("password");
        owner.setRole(Role.USER);
        owner = userRepository.save(owner);

        otherOwner = new User();
        otherOwner.setEmail("other@example.com");
        otherOwner.setPassword("password");
        otherOwner.setRole(Role.USER);
        otherOwner = userRepository.save(otherOwner);
    }

    private Project createProject(String name, User projectOwner, ProjectStatus status) {
        Project project = new Project();
        project.setName(name);
        project.setDescription("Description for " + name);
        project.setOwner(projectOwner);
        project.setStatus(status);
        return projectRepository.save(project);
    }

    @Test
    @DisplayName("Should save and retrieve a project")
    void shouldSaveAndRetrieveProject() {
        Project project = createProject("Test Project", owner, ProjectStatus.ACTIVE);

        assertThat(project.getId()).isNotNull();
        assertThat(project.getCreatedAt()).isNotNull();
        assertThat(projectRepository.findById(project.getId())).isPresent();
    }

    @Test
    @DisplayName("Should find projects by owner ID")
    void shouldFindByOwnerId() {
        createProject("Project A", owner, ProjectStatus.ACTIVE);
        createProject("Project B", owner, ProjectStatus.ARCHIVED);
        createProject("Project C", otherOwner, ProjectStatus.ACTIVE);

        List<Project> ownerProjects = projectRepository.findByOwnerId(owner.getId());

        assertThat(ownerProjects).hasSize(2);
        assertThat(ownerProjects).allMatch(p -> p.getOwner().getId().equals(owner.getId()));
    }

    @Test
    @DisplayName("Should return empty list when owner has no projects")
    void shouldReturnEmptyWhenOwnerHasNoProjects() {
        createProject("Project A", owner, ProjectStatus.ACTIVE);

        List<Project> projects = projectRepository.findByOwnerId(otherOwner.getId());

        assertThat(projects).isEmpty();
    }

    @Test
    @DisplayName("Should find projects by status")
    void shouldFindByStatus() {
        createProject("Active 1", owner, ProjectStatus.ACTIVE);
        createProject("Active 2", otherOwner, ProjectStatus.ACTIVE);
        createProject("Archived", owner, ProjectStatus.ARCHIVED);

        List<Project> activeProjects = projectRepository.findByStatus(ProjectStatus.ACTIVE);
        List<Project> archivedProjects = projectRepository.findByStatus(ProjectStatus.ARCHIVED);

        assertThat(activeProjects).hasSize(2);
        assertThat(archivedProjects).hasSize(1);
    }
}

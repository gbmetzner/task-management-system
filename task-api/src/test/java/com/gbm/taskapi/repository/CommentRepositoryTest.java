package com.gbm.taskapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.gbm.taskapi.TestContainerSupport;
import com.gbm.taskapi.config.TaskAppConfig;
import com.gbm.taskapi.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TaskAppConfig.class)
class CommentRepositoryTest extends TestContainerSupport {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserRepository userRepository;

    private User author;
    private Task task;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        author = new User();
        author.setEmail("author@example.com");
        author.setPassword("password");
        author.setRole(Role.USER);
        author = userRepository.save(author);

        Project project = new Project();
        project.setName("Test Project");
        project.setDescription("Description");
        project.setOwner(author);
        project.setStatus(ProjectStatus.ACTIVE);
        project = projectRepository.save(project);

        task = new Task();
        task.setTitle("Test Task");
        task.setProject(project);
        task.setReporter(author);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task = taskRepository.save(task);
    }

    @Test
    @DisplayName("Should save and retrieve a comment")
    void shouldSaveAndRetrieveComment() {
        Comment comment = new Comment();
        comment.setContent("This is a test comment");
        comment.setTask(task);
        comment.setAuthor(author);
        comment = commentRepository.save(comment);

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getCreatedAt()).isNotNull();
        assertThat(commentRepository.findById(comment.getId())).isPresent().hasValueSatisfying(c -> {
            assertThat(c.getContent()).isEqualTo("This is a test comment");
            assertThat(c.getTask().getId()).isEqualTo(task.getId());
            assertThat(c.getAuthor().getId()).isEqualTo(author.getId());
        });
    }

    @Test
    @DisplayName("Should delete a comment")
    void shouldDeleteComment() {
        Comment comment = new Comment();
        comment.setContent("To be deleted");
        comment.setTask(task);
        comment.setAuthor(author);
        comment = commentRepository.save(comment);

        Long commentId = comment.getId();
        commentRepository.deleteById(commentId);

        assertThat(commentRepository.findById(commentId)).isEmpty();
    }
}

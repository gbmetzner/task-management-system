package com.gbm.taskapi.repository;

import com.gbm.taskapi.model.Task;
import com.gbm.taskapi.model.TaskStatus;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Use EntityGraph to fetch assignee eagerly (solve N+1)
    @EntityGraph(attributePaths = {"assignee", "project"})
    @NullMarked
    Optional<Task> findById(Long id);

    // Find tasks by project with pagination
    @Query("SELECT t FROM Task t " + "LEFT JOIN FETCH t.assignee "
            + "LEFT JOIN FETCH t.project "
            + "WHERE t.project.id = :projectId")
    List<Task> findByProjectIdWithAssignee(@Param("projectId") Long projectId);

    // Paginated query with JOIN FETCH
    @Query(
            value = "SELECT DISTINCT t FROM Task t " + "LEFT JOIN FETCH t.assignee "
                    + "LEFT JOIN FETCH t.project "
                    + "WHERE t.project.id = :projectId",
            countQuery = "SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    Page<Task> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    // Find all tasks with assignee (no N+1)
    @EntityGraph(attributePaths = {"assignee", "project"})
    @NullMarked
    List<Task> findAll();

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssigneeId(Long assigneeId);

    List<Task> findByStatus(TaskStatus status);
}

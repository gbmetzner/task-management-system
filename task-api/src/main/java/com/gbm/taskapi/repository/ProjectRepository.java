package com.gbm.taskapi.repository;

import com.gbm.taskapi.model.Project;
import com.gbm.taskapi.model.ProjectStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerId(Long ownerId);

    List<Project> findByStatus(ProjectStatus status);
}

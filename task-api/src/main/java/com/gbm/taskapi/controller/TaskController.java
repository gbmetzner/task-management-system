package com.gbm.taskapi.controller;

import com.gbm.taskapi.dto.request.CreateTaskRequest;
import com.gbm.taskapi.dto.request.UpdateTaskRequest;
import com.gbm.taskapi.dto.response.TaskResponse;
import com.gbm.taskapi.helper.TaskMapper;
import com.gbm.taskapi.model.TaskPriority;
import com.gbm.taskapi.model.TaskStatus;
import com.gbm.taskapi.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @PostMapping("/")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        var result = taskService.createTask(request);
        var taskResponse = taskMapper.toTaskResponse(result);

        var location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/tasks/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(location).body(taskResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(
                taskMapper.toTaskResponse(taskService.findById(id).orElseThrow()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskById(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        var result = taskService.updateTask(id, request);
        return ResponseEntity.ok(taskMapper.toTaskResponse(result));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponse>> searchTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        var tasks = taskService
                .searchTasks(keyword, status, priority, assigneeId, pageable)
                .map(taskMapper::toTaskResponse);

        return ResponseEntity.ok(tasks);
    }
}

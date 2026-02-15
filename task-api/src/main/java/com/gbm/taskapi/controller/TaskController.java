package com.gbm.taskapi.controller;

import com.gbm.taskapi.dto.request.CreateTaskRequest;
import com.gbm.taskapi.dto.request.TaskSearchRequest;
import com.gbm.taskapi.dto.request.UpdateTaskRequest;
import com.gbm.taskapi.dto.response.TaskResponse;
import com.gbm.taskapi.exception.ResourceNotFoundException;
import com.gbm.taskapi.helper.TaskMapper;
import com.gbm.taskapi.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @PostMapping
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
        return ResponseEntity.ok(taskMapper.toTaskResponse(taskService
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"))));
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
            TaskSearchRequest request, @PageableDefault Pageable pageable) {

        var tasks = taskService
                .searchTasks(request.keyword(), request.status(), request.priority(), request.assigneeId(), pageable)
                .map(taskMapper::toTaskResponse);

        return ResponseEntity.ok(tasks);
    }
}

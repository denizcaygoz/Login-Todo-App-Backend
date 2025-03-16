package com.login_todo_app_backend.payload.response;

import java.time.LocalDateTime;

public class TodoResponse {
    private Long id;
    private String task;
    private boolean completed;
    private boolean deleted;
    private LocalDateTime createdAt;

    
    public TodoResponse(Long id, String task, boolean completed, boolean deleted, LocalDateTime createdAt) {
        this.id = id;
        this.task = task;
        this.completed = completed;
        this.deleted = deleted;
        this.createdAt = createdAt;
    }

    
    public Long getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

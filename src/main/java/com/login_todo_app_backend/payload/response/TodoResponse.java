package com.login_todo_app_backend.payload.response;

public class TodoResponse {
    private String task;
    private boolean completed;

    
    public TodoResponse(String task, boolean completed) {
        this.task = task;
        this.completed = completed;
    }

    
    public String getTask() {
        return task;
    }

    public boolean isCompleted() {
        return completed;
    }
}

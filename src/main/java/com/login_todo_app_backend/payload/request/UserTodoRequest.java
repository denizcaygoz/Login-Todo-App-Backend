package com.login_todo_app_backend.payload.request;

import jakarta.validation.constraints.NotBlank;

public class UserTodoRequest {
    @NotBlank
    private String username;

    public UserTodoRequest() {
    }

    public UserTodoRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
} 
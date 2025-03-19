package com.login_todo_app_backend.payload.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import com.login_todo_app_backend.payload.response.TodoResponse;

public class UpdateTodoListRequest {
    @NotBlank
    private String username;
    
    private List<TodoResponse> todos;

    public UpdateTodoListRequest() {
    }

    public UpdateTodoListRequest(String username, List<TodoResponse> todos) {
        this.username = username;
        this.todos = todos;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<TodoResponse> getTodos() {
        return todos;
    }

    public void setTodos(List<TodoResponse> todos) {
        this.todos = todos;
    }
} 
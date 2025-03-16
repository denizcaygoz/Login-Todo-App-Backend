package com.login_todo_app_backend.payload.response;

import com.login_todo_app_backend.models.Todo;
import java.util.List;

public class UserInfoResponse {
    private String username;
    private String jwtToken;
    private List<TodoResponse> todos;

    public UserInfoResponse(String username, String jwtToken, List<TodoResponse> todos) {
        this.username = username;
        this.jwtToken = jwtToken;
        this.todos = todos;
    }

    public String getUsername() {
        return username;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public List<TodoResponse> getTodos() {
        return todos;
    }
}
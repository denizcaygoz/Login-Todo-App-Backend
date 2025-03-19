package com.login_todo_app_backend.payload.response;

import java.util.List;
import java.util.Collections;

public class UserInfoResponse {
    private String username;
    private String jwtToken;
    private List<TodoResponse> todos;

    public UserInfoResponse(String username, String jwtToken, List<TodoResponse> todos) {
        this.username = username;
        this.jwtToken = jwtToken;
        this.todos = todos;
    }
    
    public UserInfoResponse(String jwtToken) {
        this.jwtToken = jwtToken;
        this.username = null;
        this.todos = Collections.emptyList();
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
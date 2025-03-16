package com.login_todo_app_backend.payload.response;

public class UserResponse {
    private String jwtToken; 

    public UserResponse(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getJwtToken() { 
        return jwtToken; 
    }
}

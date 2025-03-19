package com.login_todo_app_backend.payload.response;

public class TokenRefreshResponse {
    private String jwtToken;

    public TokenRefreshResponse(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getJwtToken() {
        return jwtToken;
    }
} 
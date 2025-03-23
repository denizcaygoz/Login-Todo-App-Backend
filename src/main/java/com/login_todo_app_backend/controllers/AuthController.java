package com.login_todo_app_backend.controllers;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.login_todo_app_backend.models.User;
import com.login_todo_app_backend.payload.request.LoginRequest;
import com.login_todo_app_backend.payload.request.SignupRequest;
import com.login_todo_app_backend.payload.response.MessageResponse;
import com.login_todo_app_backend.payload.response.TodoResponse;
import com.login_todo_app_backend.payload.response.TokenRefreshResponse;
import com.login_todo_app_backend.payload.response.UserInfoResponse;
import com.login_todo_app_backend.repository.TodoRepository;
import com.login_todo_app_backend.repository.UserRepository;
import com.login_todo_app_backend.security.jwt.JwtUtils;
import com.login_todo_app_backend.security.services.UserDetailsImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600) // Allow mobile requests
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
   
    @Autowired
    private UserRepository userRepository;
   
    @Autowired
    private TodoRepository todoRepository;
   
    @Autowired
    private PasswordEncoder encoder;
   
    @Autowired
    private JwtUtils jwtUtils;
   
    /**
     * Handles user login
     * POST <http://localhost:8080/auth/signin>
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    //Authenticating user
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    //Generating new JWT Token
    String jwt = jwtUtils.generateToken(userDetails.getUsername());

    //Getting user's todos and convert them to DTOs
    List<TodoResponse> todos = todoRepository.findByUserId(userDetails.getId())
        .stream()
        .map(todo -> new TodoResponse(todo.getTask(), todo.isCompleted()))
        .toList();

    return ResponseEntity.ok(new UserInfoResponse(
        userDetails.getUsername(),
        jwt,
        todos));
    }

   
    /**
     * Handles user registration
     * POST <http://localhost:8080/auth/register>
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken"));
        }
   
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use"));
        }
   
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
   
        userRepository.save(user);

        //Generating JWT Token for the new user
        String jwt = jwtUtils.generateToken(user.getUsername());

        return ResponseEntity.ok(new UserInfoResponse(
            user.getUsername(),
            jwt,
            List.of() 
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid Authorization header"));
        }

        //Extracting token
        String recievedToken = authHeader.substring(7);
        
        try {
            //Verify token's signature 
            if (!jwtUtils.validateJwtToken(recievedToken) && !jwtUtils.isTokenExpired(recievedToken)) {
                // If token is invalid and not just expired, reject it
                return ResponseEntity.status(401).body(new MessageResponse("Error: Invalid token"));
            }
            
            /*//Checking if token is expired
            if (!jwtUtils.isTokenExpired(recievedToken)) {
                return ResponseEntity.ok(new MessageResponse("Token is not expired yet"));
            }*/
            
            //Getting username from token
            String usernameFromToken = jwtUtils.getUserNameFromJwtToken(recievedToken);
            
            //Checking if user exists
            User user = userRepository.findByUsername(usernameFromToken)
                .orElseThrow(() -> new RuntimeException("Error: User not found"));
            
            //Generating a new token
            String newToken = jwtUtils.generateToken(usernameFromToken);
            
            //Retruns the new token
            return ResponseEntity.ok(new TokenRefreshResponse(newToken));
            
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new MessageResponse(
                "Error: Could not refresh token. Log in again. " + e.getMessage()
            ));
        }
    }
}   

package com.login_todo_app_backend.controllers;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.login_todo_app_backend.models.User;
import com.login_todo_app_backend.payload.request.LoginRequest;
import com.login_todo_app_backend.payload.request.SignupRequest;
import com.login_todo_app_backend.payload.response.UserInfoResponse;
import com.login_todo_app_backend.payload.response.MessageResponse;
import com.login_todo_app_backend.payload.response.TodoResponse;
import com.login_todo_app_backend.repository.UserRepository;
import com.login_todo_app_backend.repository.TodoRepository;
import com.login_todo_app_backend.security.jwt.JwtUtils;
import com.login_todo_app_backend.security.services.UserDetailsImpl;

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
   
    @GetMapping("/validate")
    public ResponseEntity<?> validateTokenAndGetUserData(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);

            if (jwtUtils.validateJwtToken(token)) {
                String username = jwtUtils.getUserNameFromJwtToken(token);
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Error: User not found."));

                //Converting todos to DTO format
                List<TodoResponse> todos = todoRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .map(todo -> new TodoResponse(todo.getId(), todo.getTask(), todo.isCompleted(), todo.isDeleted(), todo.getCreatedAt()))
                    .toList();

                //Generating a new token
                String newToken = jwtUtils.generateToken(username);

                return ResponseEntity.ok(new UserInfoResponse(
                    username,
                    newToken,
                    todos
                ));
            }
        }

        return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid or missing token"));
    }

   
    /**
     * Handles user login
     * POST <http://localhost:8080/auth/signin>
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
    //Validating JWT
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        if (!jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid JWT token"));
        }

        String usernameFromToken = jwtUtils.getUserNameFromJwtToken(token);
        if (!usernameFromToken.equals(loginRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Token username mismatch"));
        }
    }

    //Authenticate user
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    //Generating new JWT Token
    String jwt = jwtUtils.generateToken(userDetails.getUsername());

    //Getting user's todos and convert them to DTOs
    List<TodoResponse> todos = todoRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userDetails.getId())
        .stream()
        .map(todo -> new TodoResponse(todo.getId(), todo.getTask(), todo.isCompleted(), todo.isDeleted(), todo.getCreatedAt()))
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
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }
   
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
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
}   

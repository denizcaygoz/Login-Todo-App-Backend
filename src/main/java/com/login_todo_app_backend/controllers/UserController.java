package com.login_todo_app_backend.controllers;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import com.login_todo_app_backend.models.User;
import com.login_todo_app_backend.models.Todo;
import com.login_todo_app_backend.payload.request.UpdateTodoListRequest;
import com.login_todo_app_backend.payload.response.MessageResponse;
import com.login_todo_app_backend.payload.response.TodoResponse;
import com.login_todo_app_backend.repository.UserRepository;
import com.login_todo_app_backend.repository.TodoRepository;
import com.login_todo_app_backend.security.jwt.JwtUtils;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TodoRepository todoRepository;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @GetMapping("/todolist")
    public ResponseEntity<?> getUserTodoList(@RequestParam String username,
                                            @RequestHeader("Authorization") String authHeader) {
        //Validating token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Authorization header missing or invalid"));
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid token"));
        }
        
        //Validating if username has the right token
        String usernameFromToken = jwtUtils.getUserNameFromJwtToken(token);
        if (!usernameFromToken.equals(username)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Token username mismatch"));
        }
        
        //Fetching todos for the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found"));
        
        //Converting todos to DTO format to return back to frontend
        List<TodoResponse> todos = todoRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(todo -> new TodoResponse(
                    todo.getId(), 
                    todo.getTask(), 
                    todo.isCompleted(), 
                    todo.isDeleted(), 
                    todo.getCreatedAt()))
                .toList();
        
        //Returning empty list if user doesnt have any
        if (todos.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        return ResponseEntity.ok(todos);
    }
    
    @PostMapping("/todolist")
    @Transactional
    public ResponseEntity<?> updateTodoList(@RequestBody UpdateTodoListRequest request,
                                           @RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Authorization header missing or invalid"));
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid token"));
        }
        
        String usernameFromToken = jwtUtils.getUserNameFromJwtToken(token);
        if (!usernameFromToken.equals(request.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Token username mismatch"));
        }
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: User not found"));
        
        //Deleting all existing todos 
        List<Todo> existingTodos = todoRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(user.getId());
        for (Todo todo : existingTodos) {
            todo.setDeleted(true);
            todoRepository.save(todo);
        }
        
        //Creating new todos from the request
        List<Todo> newTodos = new ArrayList<>();
        if (request.getTodos() != null) {
            for (TodoResponse todoRequest : request.getTodos()) {
                Todo todo = new Todo();
                todo.setUser(user);
                todo.setTask(todoRequest.getTask());
                todo.setCompleted(todoRequest.isCompleted());
                todo.setDeleted(false);
                newTodos.add(todoRepository.save(todo));
            }
        }
        
        //Converting and returning the new todos
        List<TodoResponse> todoResponses = newTodos.stream()
                .map(todo -> new TodoResponse(
                    todo.getId(),
                    todo.getTask(),
                    todo.isCompleted(),
                    todo.isDeleted(),
                    todo.getCreatedAt()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(todoResponses);
    }
} 
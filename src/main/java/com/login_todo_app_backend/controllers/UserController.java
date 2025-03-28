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
    public ResponseEntity<?> getUserTodoList(@RequestHeader("Authorization") String authHeader) {
        //Validating token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Authorization header missing or invalid"));
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid token"));
        }
        
        //Extract username from token
        String username = jwtUtils.getUserNameFromJwtToken(token);
        
        //Fetching todos for the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found"));
        
        //Converting todos to DTO format to return back to frontend
        List<TodoResponse> todos = todoRepository.findByUserId((user.getId()))
                .stream()
                .map(todo -> new TodoResponse(
                    todo.getTask(), 
                    todo.isCompleted()))
                .toList();
        
        //Returning empty list if user doesnt have any
        if (todos.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        return ResponseEntity.ok(todos);
    }
    
    @PostMapping("/todolist")
    @Transactional
    public ResponseEntity<?> updateTodoList(@RequestBody List<TodoResponse> todos,
                                           @RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Authorization header missing or invalid"));
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid token"));
        }
        
        //Getting username info from token
        String username = jwtUtils.getUserNameFromJwtToken(token);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found"));
        
        //Deleting all existing todos 
        todoRepository.deleteByUserId(user.getId());
        
        //Creating new todos from the request
        List<Todo> newTodos = new ArrayList<>();
        if (todos != null) {
            for (TodoResponse todoRequest : todos) {
                Todo todo = new Todo();
                todo.setUser(user);
                todo.setTask(todoRequest.getTask());
                todo.setCompleted(todoRequest.isCompleted());
                newTodos.add(todoRepository.save(todo));
            }
        }
        
        //Converting and returning the new todos
        List<TodoResponse> todoResponses = newTodos.stream()
                .map(todo -> new TodoResponse(
                    todo.getTask(),
                    todo.isCompleted()
                    ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(todoResponses);
    }
} 
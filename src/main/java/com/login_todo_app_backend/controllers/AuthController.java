package com.login_todo_app_backend.controllers;
import java.util.List;
import java.util.stream.Collectors;
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
import com.login_todo_app_backend.payload.response.UserResponse;
import com.login_todo_app_backend.payload.response.MessageResponse;
import com.login_todo_app_backend.repository.UserRepository;
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
    private PasswordEncoder encoder;
   
    @Autowired
    private JwtUtils jwtUtils;
   
    /**
     * Handles user login
     * POST <http://localhost:8080/auth/signin>
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
   
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
   
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
   
        if (userDetails == null || userDetails.getUsername() == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Authentication failed."));
        }
        //Generate JWT Token
        String jwt = jwtUtils.generateToken(userDetails.getUsername());
   
        List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());
   
        //Send JWT Token in Response
        /*return ResponseEntity.ok(new UserInfoResponse(
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail(),
            roles,
            jwt
        ));*/
        return ResponseEntity.ok(new UserResponse(jwt));
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
   
        userRepository.saveAndFlush(user);

        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(signUpRequest.getUsername(), signUpRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate JWT Token
        String jwt = jwtUtils.generateToken(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());

        // Send JWT Token in Response
        return ResponseEntity.ok(new UserResponse(jwt));
    }
}   

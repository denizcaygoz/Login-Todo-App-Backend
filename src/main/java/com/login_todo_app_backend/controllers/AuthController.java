package com.login_todo_app_backend.controllers;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import com.login_todo_app_backend.models.ERole;
import com.login_todo_app_backend.models.Role;
import com.login_todo_app_backend.models.User;
import com.login_todo_app_backend.payload.request.LoginRequest;
import com.login_todo_app_backend.payload.request.SignupRequest;
import com.login_todo_app_backend.payload.response.UserInfoResponse;
import com.login_todo_app_backend.payload.response.MessageResponse;
import com.login_todo_app_backend.repository.RoleRepository;
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
    private RoleRepository roleRepository;
   
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
   
        //Generate JWT Token
        String jwt = jwtUtils.generateToken(userDetails.getUsername());
   
        List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());
   
        //Send JWT Token in Response
        return ResponseEntity.ok(new UserInfoResponse(
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail(),
            roles,
            jwt
        ));
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
   
        //Create new user with hashed password
        User user = new User(
            signUpRequest.getUsername(),
            signUpRequest.getEmail(),
            encoder.encode(signUpRequest.getPassword()),
            new HashSet<>()
            );
   
        //Assign roles
        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();
   
        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        roles.add(roleRepository.findByName(ERole.ROLE_ADMIN)
                              .orElseThrow(() -> new RuntimeException("Error: Role is not found.")));
                        break;
                    case "mod":
                        roles.add(roleRepository.findByName(ERole.ROLE_MODERATOR)
                              .orElseThrow(() -> new RuntimeException("Error: Role is not found.")));
                        break;
                    default:
                        roles.add(roleRepository.findByName(ERole.ROLE_USER)
                              .orElseThrow(() -> new RuntimeException("Error: Role is not found.")));
                }
            });
        }
   
        user.setRoles(roles);
        userRepository.save(user);
   
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}   

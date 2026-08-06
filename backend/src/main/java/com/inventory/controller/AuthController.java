package com.inventory.controller;

import com.inventory.entity.User;
import com.inventory.security.JwtService;
import com.inventory.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
    User user = userService.registerUser(
            request.getFullName(),
            request.getEmail(),
            request.getPassword(),
            User.Role.STAFF
    );
    return ResponseEntity.ok(user);
}

@PostMapping("/register-privileged")
public ResponseEntity<User> registerPrivileged(@RequestBody RegisterRequest request) {
    User user = userService.registerUser(
            request.getFullName(),
            request.getEmail(),
            request.getPassword(),
            request.getRole()
    );
    return ResponseEntity.ok(user);
}

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(request.getEmail())
                .password("")
                .authorities(java.util.List.of())
                .build();

        String token = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Getter
    @Setter
    static class RegisterRequest {
        private String fullName;
        private String email;
        private String password;
        private User.Role role;
    }

    @Getter
    @Setter
    static class LoginRequest {
        private String email;
        private String password;
    }

    @Getter
    static class AuthResponse {
        private final String token;
        public AuthResponse(String token) {
            this.token = token;
        }
    }
}
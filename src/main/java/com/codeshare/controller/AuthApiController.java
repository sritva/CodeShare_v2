package com.codeshare.controller;

import com.codeshare.security.JwtUtil;
import com.codeshare.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthApiController(AuthService authService,
                              AuthenticationManager authenticationManager,
                              JwtUtil jwtUtil) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String confirmPassword = body.get("confirmPassword");

        if (username == null || username.trim().length() < 3) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Username must be at least 3 characters"));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Password must be at least 6 characters"));
        }
        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Passwords do not match"));
        }

        try {
            authService.register(username, password);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
            String token = jwtUtil.generateToken(username);
            return ResponseEntity.ok(Map.of(
                "token", token,
                "username", username
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Map.of("username", userDetails.getUsername()));
    }
}

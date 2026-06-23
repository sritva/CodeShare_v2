package com.codeshare.service;

import com.codeshare.model.User;
import com.codeshare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // No global stubs — each test sets up only what it needs
    }

    // ── Happy path ──────────────────────────────────────────────────────────────

    @Test
    void register_success_returnsUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.existsByUsername("michael")).thenReturn(false);

        User result = authService.register("michael", "password123");

        assertNotNull(result);
        assertEquals("michael", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    // ── Username validation ──────────────────────────────────────────────────────

    @Test
    void register_blankUsername_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("  ", "password123"));
        assertTrue(ex.getMessage().toLowerCase().contains("username"));
    }

    @Test
    void register_tooShortUsername_throws() {
        // "ab" is only 2 characters — below the 3-char minimum
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("ab", "password123"));
        assertTrue(ex.getMessage().contains("3"));
    }

    @Test
    void register_invalidUsernameChars_throws() {
        // Spaces are not allowed
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("bad username!", "password123"));
        assertNotNull(ex.getMessage());
    }

    // ── Password validation ──────────────────────────────────────────────────────

    @Test
    void register_tooShortPassword_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("michael", "abc"));
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }

    // ── Duplicate username ───────────────────────────────────────────────────────

    @Test
    void register_duplicateUsername_throws() {
        when(userRepository.existsByUsername("michael")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register("michael", "password123"));
        assertTrue(ex.getMessage().toLowerCase().contains("taken"));
    }
}

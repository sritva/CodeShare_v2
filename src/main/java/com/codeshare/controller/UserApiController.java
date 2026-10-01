package com.codeshare.controller;

import com.codeshare.model.User;
import com.codeshare.model.Snippet;
import com.codeshare.repository.UserRepository;
import com.codeshare.service.SnippetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserRepository userRepository;
    private final SnippetService snippetService;

    public UserApiController(UserRepository userRepository, SnippetService snippetService) {
        this.userRepository = userRepository;
        this.snippetService = snippetService;
    }

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        List<Snippet> publicSnippets = snippetService.getPublicByUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("bio", user.getBio() != null ? user.getBio() : "");
        response.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        response.put("githubUrl", user.getGithubUrl() != null ? user.getGithubUrl() : "");
        response.put("linkedinUrl", user.getLinkedinUrl() != null ? user.getLinkedinUrl() : "");
        response.put("snippets", publicSnippets);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        if (currentUser == null) return ResponseEntity.status(401).build();

        String bio = request.get("bio") != null && !request.get("bio").trim().isEmpty() ? request.get("bio").trim() : null;
        if (bio != null && bio.length() > 1000) {
            return ResponseEntity.badRequest().body(Map.of("error", "bio must be at most 1000 characters"));
        }

        String avatarUrl = request.get("avatarUrl") != null && !request.get("avatarUrl").trim().isEmpty() ? request.get("avatarUrl").trim() : null;
        String githubUrl = request.get("githubUrl") != null && !request.get("githubUrl").trim().isEmpty() ? request.get("githubUrl").trim() : null;
        String linkedinUrl = request.get("linkedinUrl") != null && !request.get("linkedinUrl").trim().isEmpty() ? request.get("linkedinUrl").trim() : null;

        try {
            validateUrl(avatarUrl, "avatarUrl");
            validateUrl(githubUrl, "githubUrl");
            validateUrl(linkedinUrl, "linkedinUrl");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        currentUser.setBio(bio);
        currentUser.setAvatarUrl(avatarUrl);
        currentUser.setGithubUrl(githubUrl);
        currentUser.setLinkedinUrl(linkedinUrl);

        userRepository.save(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("username", currentUser.getUsername());
        response.put("bio", currentUser.getBio());
        response.put("avatarUrl", currentUser.getAvatarUrl());
        response.put("githubUrl", currentUser.getGithubUrl());
        response.put("linkedinUrl", currentUser.getLinkedinUrl());

        return ResponseEntity.ok(response);
    }

    private void validateUrl(String url, String fieldName) {
        if (url == null) return;
        if (url.length() > 255) {
            throw new IllegalArgumentException(fieldName + " must be at most 255 characters");
        }
        try {
            java.net.URI uri = new java.net.URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException(fieldName + " must use http or https scheme");
            }
            if (host == null || host.trim().isEmpty()) {
                throw new IllegalArgumentException(fieldName + " must have a non-empty host");
            }
        } catch (java.net.URISyntaxException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format");
        }
    }
}

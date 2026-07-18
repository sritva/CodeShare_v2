package com.codeshare.controller;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import com.codeshare.repository.UserRepository;
import com.codeshare.service.GeminiClient;
import com.codeshare.service.RateLimiterService;
import com.codeshare.service.SnippetService;
import com.codeshare.dto.SnippetRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/snippets")
public class SnippetApiController {
    private static final Logger log = LoggerFactory.getLogger(SnippetApiController.class);

    private final SnippetService snippetService;
    private final UserRepository userRepository;
    private final GeminiClient geminiClient;
    private final RateLimiterService rateLimiterService;

    public SnippetApiController(SnippetService snippetService,
                                 UserRepository userRepository,
                                 GeminiClient geminiClient,
                                 RateLimiterService rateLimiterService) {
        this.snippetService = snippetService;
        this.userRepository = userRepository;
        this.geminiClient = geminiClient;
        this.rateLimiterService = rateLimiterService;
    }

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(
            userDetails.getUsername()).orElse(null);
    }

    @GetMapping("/public")
    public ResponseEntity<?> getPublic(
            @RequestParam(defaultValue = "0") int page) {
        Page<Snippet> snippetPage = snippetService.getAllPublic(page);
        return ResponseEntity.ok(Map.of(
            "snippets", snippetPage.getContent(),
            "totalPages", snippetPage.getTotalPages(),
            "currentPage", page,
            "hasNext", snippetPage.hasNext(),
            "hasPrevious", snippetPage.hasPrevious()
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "0") int page) {
        String lang = "all".equalsIgnoreCase(language) ? "" : language;
        Page<Snippet> result = snippetService.search(keyword, lang, page);
        return ResponseEntity.ok(Map.of(
            "snippets", result.getContent(),
            "totalPages", result.getTotalPages(),
            "totalResults", result.getTotalElements(),
            "currentPage", page
        ));
    }

    @GetMapping("/mine")
    public ResponseEntity<?> getMySnippets(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(snippetService.getByUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Snippet snippet = snippetService.getById(id);
        if (!snippet.isPublic()) {
            User user = getCurrentUser(userDetails);
            if (user == null || !snippet.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).build();
            }
        }
        return ResponseEntity.ok(snippet);
    }

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody SnippetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();

        Snippet snippet = new Snippet();
        snippet.setTitle(request.getTitle());
        snippet.setCode(request.getCode());
        snippet.setLanguage(request.getLanguage() != null ? request.getLanguage() : "text");
        snippet.setPublic(request.isPublic());

        Snippet saved = snippetService.create(snippet, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody SnippetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();

        Snippet details = new Snippet();
        details.setTitle(request.getTitle());
        details.setCode(request.getCode());
        details.setLanguage(request.getLanguage() != null ? request.getLanguage() : "text");
        details.setPublic(request.isPublic());

        try {
            snippetService.update(id, details, user);
            return ResponseEntity.ok(Map.of("message", "Updated successfully"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403)
                .body(Map.of("error", "Not your snippet"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();

        try {
            snippetService.delete(id, user);
            return ResponseEntity.ok(
                Map.of("message", "Deleted successfully"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403)
                .body(Map.of("error", "Not your snippet"));
        }
    }

    @PostMapping("/{id}/explain")
    public ResponseEntity<?> explain(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();

        Snippet snippet = snippetService.getById(id);

        if (!snippet.isPublic() && 
            !snippet.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        if (snippet.getAiExplanation() != null && 
            !snippet.getAiExplanation().trim().isEmpty()) {
            return ResponseEntity.ok(
                Map.of("explanation", snippet.getAiExplanation()));
        }

        if (!rateLimiterService.allowRequest((long) user.getId())) {
            return ResponseEntity.status(429)
                .body(Map.of("error", 
                    "Rate limit exceeded. Max 5 requests per minute."));
        }

        try {
            String explanation = geminiClient.generateExplanation(
                snippet.getCode(), snippet.getLanguage());
            snippetService.updateExplanation(id, explanation);
            return ResponseEntity.ok(Map.of("explanation", explanation));
        } catch (Exception e) {
            log.error("Failed to generate AI explanation for snippet ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to generate explanation"));
        }
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<?> enableSharing(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();

        try {
            Snippet snippet = snippetService.enableSharing(id, user);
            return ResponseEntity.ok(Map.of(
                "shareToken", snippet.getShareToken(),
                "shareEnabled", snippet.isShareEnabled()
            ));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/unshare")
    public ResponseEntity<?> disableSharing(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();

        try {
            snippetService.disableSharing(id, user);
            return ResponseEntity.ok(
                Map.of("message", "Sharing disabled"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/shared/{token}")
    public ResponseEntity<?> getSharedSnippet(
            @PathVariable String token) {
        try {
            Snippet snippet = snippetService.getByShareToken(token);
            return ResponseEntity.ok(new com.codeshare.dto.SharedSnippetResponse(
                snippet.getTitle(),
                snippet.getCode(),
                snippet.getLanguage(),
                snippet.getUsername(),
                snippet.getCreatedAt(),
                snippet.getUpdatedAt(),
                snippet.getAiExplanation()
            ));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("error", e.getReason()));
        }
    }
}

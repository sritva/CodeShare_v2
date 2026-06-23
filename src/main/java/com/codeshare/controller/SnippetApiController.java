package com.codeshare.controller;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import com.codeshare.repository.UserRepository;
import com.codeshare.service.GeminiClient;
import com.codeshare.service.RateLimiterService;
import com.codeshare.service.SnippetService;
import jakarta.servlet.http.HttpSession;
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
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();

        String title = (String) body.get("title");
        String code = (String) body.get("code");
        String language = (String) body.get("language");
        boolean isPublic = body.get("isPublic") == null 
            || (boolean) body.get("isPublic");

        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Title cannot be blank"));
        }
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Code cannot be blank"));
        }

        Snippet snippet = new Snippet();
        snippet.setTitle(title);
        snippet.setCode(code);
        snippet.setLanguage(language != null ? language : "text");
        snippet.setPublic(isPublic);

        Snippet saved = snippetService.create(snippet, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();

        String title = (String) body.get("title");
        String code = (String) body.get("code");
        String language = (String) body.get("language");
        boolean isPublic = body.get("isPublic") == null 
            || (boolean) body.get("isPublic");

        Snippet details = new Snippet();
        details.setTitle(title);
        details.setCode(code);
        details.setLanguage(language != null ? language : "text");
        details.setPublic(isPublic);

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
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session) {
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

        if (!rateLimiterService.allowRequest(session.getId())) {
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
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to generate explanation"));
        }
    }
}

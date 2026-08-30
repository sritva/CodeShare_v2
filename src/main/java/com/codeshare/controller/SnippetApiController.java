package com.codeshare.controller;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import com.codeshare.repository.UserRepository;
import com.codeshare.service.GeminiClient;
import com.codeshare.service.RateLimiterService;
import com.codeshare.service.SnippetService;
import com.codeshare.service.CommentService;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/snippets")
public class SnippetApiController {
    private static final Logger log = LoggerFactory.getLogger(SnippetApiController.class);

    private final SnippetService snippetService;
    private final UserRepository userRepository;
    private final GeminiClient geminiClient;
    private final RateLimiterService rateLimiterService;
    private final CommentService commentService;

    public SnippetApiController(SnippetService snippetService,
                                 UserRepository userRepository,
                                 GeminiClient geminiClient,
                                 RateLimiterService rateLimiterService,
                                 CommentService commentService) {
        this.snippetService = snippetService;
        this.userRepository = userRepository;
        this.geminiClient = geminiClient;
        this.rateLimiterService = rateLimiterService;
        this.commentService = commentService;
    }

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByUsername(
            userDetails.getUsername()).orElse(null);
    }

    @GetMapping("/public")
    public ResponseEntity<?> getPublic(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetails userDetails) {
        Page<Snippet> snippetPage = snippetService.getAllPublic(page);
        User current = getCurrentUser(userDetails);
        
        List<?> mapped = snippetPage.getContent().stream().map(s -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", s.getId());
            m.put("title", s.getTitle());
            m.put("code", s.getCode());
            m.put("language", s.getLanguage());
            m.put("public", s.isPublic());
            m.put("username", s.getUsername());
            m.put("createdAt", s.getCreatedAt());
            m.put("likesCount", s.getLikedBy().size());
            m.put("liked", current != null && s.getLikedBy().stream().anyMatch(u -> u.getId().equals(current.getId())));
            m.put("starsCount", s.getStarredBy().size());
            m.put("starred", current != null && s.getStarredBy().stream().anyMatch(u -> u.getId().equals(current.getId())));
            m.put("parentIdValue", s.getParentIdValue());
            m.put("parentTitle", s.getParentTitle());
            m.put("parentUsername", s.getParentUsername());
            return m;
        }).toList();

        return ResponseEntity.ok(Map.of(
            "snippets", mapped,
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
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal UserDetails userDetails) {
        String lang = "all".equalsIgnoreCase(language) ? "" : language;
        Page<Snippet> result = snippetService.search(keyword, lang, page);
        User current = getCurrentUser(userDetails);

        List<?> mapped = result.getContent().stream().map(s -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", s.getId());
            m.put("title", s.getTitle());
            m.put("code", s.getCode());
            m.put("language", s.getLanguage());
            m.put("public", s.isPublic());
            m.put("username", s.getUsername());
            m.put("createdAt", s.getCreatedAt());
            m.put("likesCount", s.getLikedBy().size());
            m.put("liked", current != null && s.getLikedBy().stream().anyMatch(u -> u.getId().equals(current.getId())));
            m.put("starsCount", s.getStarredBy().size());
            m.put("starred", current != null && s.getStarredBy().stream().anyMatch(u -> u.getId().equals(current.getId())));
            m.put("parentIdValue", s.getParentIdValue());
            m.put("parentTitle", s.getParentTitle());
            m.put("parentUsername", s.getParentUsername());
            return m;
        }).toList();

        return ResponseEntity.ok(Map.of(
            "snippets", mapped,
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

        List<Snippet> list = snippetService.getByUser(user);
        List<?> mapped = list.stream().map(s -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", s.getId());
            m.put("title", s.getTitle());
            m.put("code", s.getCode());
            m.put("language", s.getLanguage());
            m.put("public", s.isPublic());
            m.put("shareEnabled", s.isShareEnabled());
            m.put("username", s.getUsername());
            m.put("createdAt", s.getCreatedAt());
            m.put("likesCount", s.getLikedBy().size());
            m.put("starsCount", s.getStarredBy().size());
            m.put("parentIdValue", s.getParentIdValue());
            m.put("parentTitle", s.getParentTitle());
            m.put("parentUsername", s.getParentUsername());
            return m;
        }).toList();

        return ResponseEntity.ok(mapped);
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
        User current = getCurrentUser(userDetails);
        boolean liked = current != null && snippet.getLikedBy().stream()
                .anyMatch(u -> u.getId().equals(current.getId()));
        boolean starred = current != null && snippet.getStarredBy().stream()
                .anyMatch(u -> u.getId().equals(current.getId()));

        java.util.Map<String, Object> responseMap = new java.util.HashMap<>();
        responseMap.put("id", snippet.getId());
        responseMap.put("title", snippet.getTitle());
        responseMap.put("code", snippet.getCode());
        responseMap.put("language", snippet.getLanguage());
        responseMap.put("public", snippet.isPublic());
        responseMap.put("username", snippet.getUsername());
        responseMap.put("createdAt", snippet.getCreatedAt());
        responseMap.put("updatedAt", snippet.getUpdatedAt());
        responseMap.put("shareToken", snippet.getShareToken() != null ? snippet.getShareToken() : "");
        responseMap.put("shareEnabled", snippet.isShareEnabled());
        responseMap.put("aiExplanation", snippet.getAiExplanation() != null ? snippet.getAiExplanation() : "");
        responseMap.put("likesCount", snippet.getLikedBy().size());
        responseMap.put("liked", liked);
        responseMap.put("starsCount", snippet.getStarredBy().size());
        responseMap.put("starred", starred);
        responseMap.put("parentId", snippet.getParentIdValue());
        responseMap.put("parentTitle", snippet.getParentTitle());
        responseMap.put("parentUsername", snippet.getParentUsername());

        return ResponseEntity.ok(responseMap);
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
            String message = (e.getMessage() != null && !e.getMessage().trim().isEmpty())
                    ? e.getMessage()
                    : "Failed to generate explanation";
            return ResponseEntity.status(500)
                .body(Map.of("error", message));
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
                snippet.getId(),
                snippet.getTitle(),
                snippet.getCode(),
                snippet.getLanguage(),
                snippet.getUsername(),
                snippet.getCreatedAt(),
                snippet.getUpdatedAt(),
                snippet.getAiExplanation(),
                snippet.getParentIdValue(),
                snippet.getParentTitle(),
                snippet.getParentUsername()
            ));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("error", e.getReason()));
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeSnippet(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();
        snippetService.likeSnippet(id, user);
        return ResponseEntity.ok(Map.of("message", "Liked successfully"));
    }

    @PostMapping("/{id}/unlike")
    public ResponseEntity<?> unlikeSnippet(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();
        snippetService.unlikeSnippet(id, user);
        return ResponseEntity.ok(Map.of("message", "Unliked successfully"));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable Integer id) {
        Snippet snippet = snippetService.getById(id);
        List<com.codeshare.model.Comment> comments = commentService.getCommentsForSnippet(snippet);
        List<?> commentPayloads = comments.stream().map(c -> Map.of(
            "id", c.getId(),
            "content", c.getContent(),
            "createdAt", c.getCreatedAt(),
            "username", c.getUsername()
        )).toList();
        return ResponseEntity.ok(commentPayloads);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();
        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Comment content cannot be empty"));
        }
        Snippet snippet = snippetService.getById(id);
        com.codeshare.model.Comment comment = commentService.addComment(snippet, user, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "id", comment.getId(),
            "content", comment.getContent(),
            "createdAt", comment.getCreatedAt(),
            "username", comment.getUsername()
        ));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Integer commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();
        try {
            commentService.deleteComment(commentId, user);
            return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/fork")
    public ResponseEntity<?> forkSnippet(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();

        try {
            Snippet forked = snippetService.fork(id, user);
            java.util.Map<String, Object> responseMap = new java.util.HashMap<>();
            responseMap.put("id", forked.getId());
            responseMap.put("title", forked.getTitle());
            responseMap.put("message", "Snippet forked successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/star")
    public ResponseEntity<?> starSnippet(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();
        snippetService.starSnippet(id, user);
        return ResponseEntity.ok(Map.of("message", "Starred successfully"));
    }

    @PostMapping("/{id}/unstar")
    public ResponseEntity<?> unstarSnippet(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();
        snippetService.unstarSnippet(id, user);
        return ResponseEntity.ok(Map.of("message", "Unstarred successfully"));
    }

    @GetMapping("/starred")
    public ResponseEntity<?> getStarredSnippets(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        if (user == null) return ResponseEntity.status(401).build();
        
        List<Snippet> list = snippetService.getStarredByUser(user);
        List<?> mapped = list.stream().map(s -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", s.getId());
            m.put("title", s.getTitle());
            m.put("code", s.getCode());
            m.put("language", s.getLanguage());
            m.put("public", s.isPublic());
            m.put("username", s.getUsername());
            m.put("createdAt", s.getCreatedAt());
            m.put("likesCount", s.getLikedBy().size());
            m.put("starsCount", s.getStarredBy().size());
            m.put("starred", true);
            m.put("parentIdValue", s.getParentIdValue());
            m.put("parentTitle", s.getParentTitle());
            m.put("parentUsername", s.getParentUsername());
            return m;
        }).toList();
        
        return ResponseEntity.ok(mapped);
    }
}

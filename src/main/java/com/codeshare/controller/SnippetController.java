package com.codeshare.controller;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import com.codeshare.repository.UserRepository;
import com.codeshare.service.SnippetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import com.codeshare.service.GeminiClient;
import com.codeshare.service.RateLimiterService;
import java.util.List;

@Controller
public class SnippetController {

    private final SnippetService snippetService;
    private final UserRepository userRepository;
    private final GeminiClient geminiClient;
    private final RateLimiterService rateLimiterService;

    @Autowired
    public SnippetController(SnippetService snippetService, UserRepository userRepository, GeminiClient geminiClient, RateLimiterService rateLimiterService) {
        this.snippetService = snippetService;
        this.userRepository = userRepository;
        this.geminiClient = geminiClient;
        this.rateLimiterService = rateLimiterService;
    }

    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Page<Snippet> snippetPage = snippetService.getAllPublic(page);
        model.addAttribute("snippets", snippetPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", snippetPage.getTotalPages());
        model.addAttribute("hasPrevious", snippetPage.hasPrevious());
        model.addAttribute("hasNext", snippetPage.hasNext());
        return "home";
    }

    @GetMapping("/my-snippets")
    public String mySnippets(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = getCurrentUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }
        List<Snippet> userSnippets = snippetService.getByUser(currentUser);
        model.addAttribute("snippets", userSnippets);
        return "my-snippets";
    }

    @GetMapping("/create-snippet")
    public String createSnippetForm() {
        return "create-snippet";
    }

    @PostMapping("/create-snippet")
    public String createSnippet(
            @RequestParam("title") String title,
            @RequestParam("code") String code,
            @RequestParam("language") String language,
            @RequestParam(value = "isPublic", required = false) String isPublicStr,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User currentUser = getCurrentUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (title == null || title.trim().isEmpty()) {
            model.addAttribute("error", "Title cannot be blank");
            return "create-snippet";
        }
        if (title.trim().length() > 200) {
            model.addAttribute("error", "Title cannot exceed 200 characters");
            return "create-snippet";
        }
        if (code == null || code.trim().isEmpty()) {
            model.addAttribute("error", "Code cannot be blank");
            return "create-snippet";
        }
        if (language == null || language.trim().isEmpty()) {
            model.addAttribute("error", "Language cannot be blank");
            return "create-snippet";
        }

        boolean isPublic = isPublicStr != null;

        Snippet snippet = new Snippet();
        snippet.setTitle(title);
        snippet.setCode(code);
        snippet.setLanguage(language);
        snippet.setPublic(isPublic);

        try {
            snippetService.create(snippet, currentUser);
            return "redirect:/my-snippets";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "create-snippet";
        }
    }

    @GetMapping("/view-snippet")
    public String viewSnippet(
            @RequestParam("id") Integer id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Snippet snippet = snippetService.getById(id);
        User currentUser = getCurrentUser(userDetails);

        // Access check: if private, only owner can view
        if (!snippet.isPublic()) {
            if (currentUser == null || !snippet.getUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Access denied to private snippet");
            }
        }

        model.addAttribute("snippet", snippet);
        return "view-snippet";
    }

    @GetMapping("/edit-snippet")
    public String editSnippetForm(
            @RequestParam("id") Integer id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Snippet snippet = snippetService.getById(id);
        User currentUser = getCurrentUser(userDetails);

        if (currentUser == null || !snippet.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Unauthorized to edit snippet");
        }

        model.addAttribute("snippet", snippet);
        return "edit-snippet";
    }

    @PostMapping("/edit-snippet")
    public String editSnippet(
            @RequestParam("id") Integer id,
            @RequestParam("title") String title,
            @RequestParam("code") String code,
            @RequestParam("language") String language,
            @RequestParam(value = "isPublic", required = false) String isPublicStr,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User currentUser = getCurrentUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (title == null || title.trim().isEmpty()) {
            model.addAttribute("error", "Title cannot be blank");
            return "edit-snippet";
        }
        if (title.trim().length() > 200) {
            model.addAttribute("error", "Title cannot exceed 200 characters");
            return "edit-snippet";
        }
        if (code == null || code.trim().isEmpty()) {
            model.addAttribute("error", "Code cannot be blank");
            return "edit-snippet";
        }
        if (language == null || language.trim().isEmpty()) {
            model.addAttribute("error", "Language cannot be blank");
            return "edit-snippet";
        }

        boolean isPublic = isPublicStr != null;

        Snippet details = new Snippet();
        details.setTitle(title);
        details.setCode(code);
        details.setLanguage(language);
        details.setPublic(isPublic);

        try {
            snippetService.update(id, details, currentUser);
            return "redirect:/my-snippets";
        } catch (IllegalArgumentException e) {
            model.addAttribute("snippet", snippetService.getById(id));
            model.addAttribute("error", e.getMessage());
            return "edit-snippet";
        }
    }

    @PostMapping("/delete-snippet")
    public String deleteSnippet(
            @RequestParam("id") Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = getCurrentUser(userDetails);
        if (currentUser == null) {
            return "redirect:/login";
        }

        snippetService.delete(id, currentUser);
        return "redirect:/my-snippets";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        // If language is "all", treat it as empty so the search service returns all languages
        String langSearch = "all".equalsIgnoreCase(language) ? "" : language;
        Page<Snippet> snippetPage = snippetService.search(keyword, langSearch, page);
        model.addAttribute("snippets", snippetPage.getContent());
        model.addAttribute("totalResults", snippetPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("language", language);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", snippetPage.getTotalPages());
        model.addAttribute("hasPrevious", snippetPage.hasPrevious());
        model.addAttribute("hasNext", snippetPage.hasNext());
        return "search";
    }

    @PostMapping("/view-snippet/explain")
    @ResponseBody
    public ResponseEntity<String> explainSnippet(
            @RequestParam("id") Integer id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session) {

        User currentUser = getCurrentUser(userDetails);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to request explanations.");
        }

        Snippet snippet = snippetService.getById(id);
        if (snippet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Snippet not found.");
        }

        // Access check: if private, only owner can request explanation
        if (!snippet.isPublic()) {
            if (!snippet.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
            }
        }

        // 1. If explanation already exists in DB, return it immediately (Cache hit)
        if (snippet.getAiExplanation() != null && !snippet.getAiExplanation().trim().isEmpty()) {
            return ResponseEntity.ok(snippet.getAiExplanation());
        }

        // 2. Enforce session-based rate limit for new generation requests
        if (!rateLimiterService.allowRequest(session.getId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded. You can request up to 5 explanations per minute.");
        }

        // 3. Request explanation from Gemini API
        try {
            String explanation = geminiClient.generateExplanation(snippet.getCode(), snippet.getLanguage());
            
            // 4. Update snippet to cache the explanation
            snippetService.updateExplanation(id, explanation);
            
            return ResponseEntity.ok(explanation);
        } catch (IllegalStateException e) {
            // Friendly error for missing API key
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate explanation: " + e.getMessage());
        }
    }
}

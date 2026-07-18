package com.codeshare.service;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import com.codeshare.repository.SnippetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class SnippetService {
    private static final Logger log = LoggerFactory.getLogger(SnippetService.class);

    private final SnippetRepository snippetRepository;

    @Autowired
    public SnippetService(SnippetRepository snippetRepository) {
        this.snippetRepository = snippetRepository;
    }

    private static final int PAGE_SIZE = 10;

    public List<Snippet> getAllPublic() {
        return snippetRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    public Page<Snippet> getAllPublic(int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        return snippetRepository.findByIsPublicTrue(pageable);
    }

    public Snippet getById(Integer id) {
        return snippetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found"));
    }

    public List<Snippet> getByUser(User user) {
        return snippetRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public Snippet create(Snippet snippet, User user) {
        if (snippet.getTitle() == null || snippet.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Snippet title cannot be empty");
        }
        if (snippet.getTitle().trim().length() > 200) {
            throw new IllegalArgumentException("Snippet title must be 200 characters or fewer");
        }
        if (snippet.getCode() == null || snippet.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Snippet code cannot be empty");
        }
        if (snippet.getLanguage() == null || snippet.getLanguage().trim().isEmpty()) {
            throw new IllegalArgumentException("Snippet language cannot be empty");
        }
        snippet.setUser(user);
        Snippet savedSnippet = snippetRepository.save(snippet);
        log.info("Snippet created - id: {}, user: {}", savedSnippet.getId(), user.getUsername());
        return savedSnippet;
    }

    @Transactional
    public Snippet update(Integer id, Snippet snippetDetails, User currentUser) {
        log.info("Snippet updated - id: {}, user: {}", id, currentUser.getUsername());
        Snippet existing = getById(id);
        
        // Ownership check
        if (!existing.getUser().getId().equals(currentUser.getId())) {
            log.warn("Unauthorized access attempt - snippet: {}, user: {}", id, currentUser.getUsername());
            throw new AccessDeniedException("Unauthorized access to update snippet");
        }

        if (snippetDetails.getTitle() == null || snippetDetails.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Snippet title cannot be empty");
        }
        if (snippetDetails.getTitle().trim().length() > 200) {
            throw new IllegalArgumentException("Snippet title must be 200 characters or fewer");
        }
        if (snippetDetails.getCode() == null || snippetDetails.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Snippet code cannot be empty");
        }
        if (snippetDetails.getLanguage() == null || snippetDetails.getLanguage().trim().isEmpty()) {
            throw new IllegalArgumentException("Snippet language cannot be empty");
        }

        existing.setTitle(snippetDetails.getTitle().trim());
        existing.setCode(snippetDetails.getCode());
        existing.setLanguage(snippetDetails.getLanguage());
        existing.setPublic(snippetDetails.isPublic());
        
        return snippetRepository.save(existing);
    }

    @Transactional
    public void delete(Integer id, User currentUser) {
        log.info("Snippet deleted - id: {}, user: {}", id, currentUser.getUsername());
        Snippet existing = getById(id);
        
        if (!existing.getUser().getId().equals(currentUser.getId())) {
            log.warn("Unauthorized access attempt - snippet: {}, user: {}", id, currentUser.getUsername());
            throw new AccessDeniedException("Unauthorized access to delete snippet");
        }

        snippetRepository.delete(existing);
    }

    public List<Snippet> search(String title, String language) {
        boolean hasTitle = title != null && !title.trim().isEmpty();
        boolean hasLanguage = language != null && !language.trim().isEmpty();

        if (hasTitle && hasLanguage) {
            return snippetRepository.findByIsPublicTrueAndTitleContainingIgnoreCaseAndLanguageIgnoreCaseOrderByCreatedAtDesc(title.trim(), language.trim());
        } else if (hasTitle) {
            return snippetRepository.findByIsPublicTrueAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(title.trim());
        } else if (hasLanguage) {
            return snippetRepository.findByIsPublicTrueAndLanguageIgnoreCaseOrderByCreatedAtDesc(language.trim());
        } else {
            return getAllPublic();
        }
    }

    public Page<Snippet> search(String title, String language, int page) {
        boolean hasTitle = title != null && !title.trim().isEmpty();
        boolean hasLanguage = language != null && !language.trim().isEmpty();
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (hasTitle && hasLanguage) {
            return snippetRepository.findByIsPublicTrueAndTitleContainingIgnoreCaseAndLanguageIgnoreCase(title.trim(), language.trim(), pageable);
        } else if (hasTitle) {
            return snippetRepository.findByIsPublicTrueAndTitleContainingIgnoreCase(title.trim(), pageable);
        } else if (hasLanguage) {
            return snippetRepository.findByIsPublicTrueAndLanguageIgnoreCase(language.trim(), pageable);
        } else {
            return getAllPublic(page);
        }
    }

    @Transactional
    public Snippet updateExplanation(Integer id, String aiExplanation) {
        Snippet existing = getById(id);
        existing.setAiExplanation(aiExplanation);
        return snippetRepository.save(existing);
    }

    @Transactional
    public Snippet enableSharing(Integer id, User requestingUser) {
        Snippet snippet = getById(id);

        if (!snippet.getUser().getId().equals(requestingUser.getId())) {
            throw new AccessDeniedException("Only the owner can share this snippet");
        }

        if (snippet.getShareToken() == null) {
            snippet.setShareToken(java.util.UUID.randomUUID().toString());
        }
        snippet.setShareEnabled(true);
        snippet.setSharedAt(java.time.LocalDateTime.now());

        log.info("Sharing enabled for snippet {} by user {}",
            id, requestingUser.getUsername());
        return snippetRepository.save(snippet);
    }

    @Transactional
    public Snippet disableSharing(Integer id, User requestingUser) {
        Snippet snippet = getById(id);

        // Only owner can disable sharing
        if (!snippet.getUser().getId().equals(requestingUser.getId())) {
            throw new AccessDeniedException(
                "Only the owner can disable sharing");
        }

        snippet.setShareEnabled(false);
        log.info("Sharing disabled for snippet {} by user {}",
            id, requestingUser.getUsername());
        return snippetRepository.save(snippet);
    }

    public Snippet getByShareToken(String token) {
        Snippet snippet = snippetRepository.findByShareToken(token)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Share link not found"));

        if (!snippet.isShareEnabled()) {
            throw new ResponseStatusException(
                HttpStatus.GONE, "This share link has been disabled");
        }
        return snippet;
    }
}

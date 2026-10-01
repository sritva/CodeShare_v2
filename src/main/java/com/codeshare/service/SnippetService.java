package com.codeshare.service;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import com.codeshare.repository.SnippetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
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

    private final jakarta.persistence.EntityManager entityManager;

    @Autowired
    public SnippetService(SnippetRepository snippetRepository, jakarta.persistence.EntityManager entityManager) {
        this.snippetRepository = snippetRepository;
        this.entityManager = entityManager;
    }

    private static final int PAGE_SIZE = 10;

    public List<Snippet> getAllPublic() {
        return snippetRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }

    @Cacheable(value = "snippets", key = "'public-page-' + #page")
    public Page<Snippet> getAllPublic(int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        return snippetRepository.findByIsPublicTrue(pageable);
    }

    public Snippet getById(Integer id) {
        return snippetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found"));
    }

    public Snippet getReadableById(Integer id, User user) {
        Snippet snippet = getById(id);
        SnippetAccessPolicy.requireRead(snippet, user);
        return snippet;
    }

    private Snippet getForUpdate(Integer id) {
        Snippet snippet = snippetRepository.findForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found"));
        // Open EntityManager in View may already hold a pre-inference snapshot.
        entityManager.refresh(snippet, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return snippet;
    }

    public List<Snippet> getByUser(User user) {
        return snippetRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    @CacheEvict(value = "snippets", allEntries = true)
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
    @CacheEvict(value = "snippets", allEntries = true)
    public Snippet update(Integer id, Snippet snippetDetails, User currentUser) {
        log.info("Snippet updated - id: {}, user: {}", id, currentUser.getUsername());
        Snippet existing = getForUpdate(id);
        
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

        if (!java.util.Objects.equals(existing.getCode(), snippetDetails.getCode())
                || !java.util.Objects.equals(existing.getLanguage(), snippetDetails.getLanguage())) {
            existing.setAiExplanation(null);
        }
        existing.setTitle(snippetDetails.getTitle().trim());
        existing.setCode(snippetDetails.getCode());
        existing.setLanguage(snippetDetails.getLanguage());
        existing.setPublic(snippetDetails.isPublic());
        
        return snippetRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "snippets", allEntries = true)
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

    @Cacheable(value = "snippets", key = "'search-' + #title + '-' + #language + '-' + #page")
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
    @CacheEvict(value = "snippets", allEntries = true)
    public Snippet updateExplanation(Integer id, String expectedCode, String expectedLanguage,
                                     String aiExplanation) {
        Snippet existing = getForUpdate(id);
        if (!java.util.Objects.equals(existing.getCode(), expectedCode)
                || !java.util.Objects.equals(existing.getLanguage(), expectedLanguage)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Snippet changed while generating the explanation. Please try again.");
        }
        existing.setAiExplanation(aiExplanation);
        return snippetRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "snippets", allEntries = true)
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
    @CacheEvict(value = "snippets", allEntries = true)
    public Snippet disableSharing(Integer id, User requestingUser) {
        Snippet snippet = getById(id);

        // Only owner can disable sharing
        if (!snippet.getUser().getId().equals(requestingUser.getId())) {
            throw new AccessDeniedException(
                "Only the owner can disable sharing");
        }

        snippet.setShareEnabled(false);
        snippet.setShareToken(null);
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

    @Transactional
    @CacheEvict(value = "snippets", allEntries = true)
    public void likeSnippet(Integer snippetId, User user) {
        Snippet snippet = getById(snippetId);
        SnippetAccessPolicy.requireInteraction(snippet, user);
        snippet.getLikedBy().add(user);
        snippetRepository.save(snippet);
    }

    @Transactional
    @CacheEvict(value = "snippets", allEntries = true)
    public void unlikeSnippet(Integer snippetId, User user) {
        Snippet snippet = getById(snippetId);
        snippet.getLikedBy().removeIf(u -> u.getId().equals(user.getId()));
        snippetRepository.save(snippet);
    }

    @Transactional
    @CacheEvict(value = "snippets", allEntries = true)
    public Snippet fork(Integer id, User user) {
        return fork(id, user, null);
    }

    @Transactional
    @CacheEvict(value = "snippets", allEntries = true)
    public Snippet fork(Integer id, User user, String shareToken) {
        Snippet parent = getById(id);
        SnippetAccessPolicy.requireFork(parent, user, shareToken);

        Snippet fork = new Snippet();
        fork.setTitle("Fork of " + parent.getTitle());
        fork.setCode(parent.getCode());
        fork.setLanguage(parent.getLanguage());
        fork.setPublic(parent.isPublic());
        fork.setUser(user);
        fork.setParent(parent);
        
        Snippet savedFork = snippetRepository.save(fork);
        log.info("Snippet forked - original id: {}, fork id: {}, user: {}", parent.getId(), savedFork.getId(), user.getUsername());
        return savedFork;
    }

    public List<Snippet> getPublicByUser(User user) {
        return snippetRepository.findByUserAndIsPublicTrueOrderByCreatedAtDesc(user);
    }

    @Transactional
    @CacheEvict(value = "snippets", allEntries = true)
    public void starSnippet(Integer snippetId, User user) {
        Snippet snippet = getById(snippetId);
        SnippetAccessPolicy.requireInteraction(snippet, user);
        snippet.getStarredBy().add(user);
        snippetRepository.save(snippet);
    }

    @Transactional
    @CacheEvict(value = "snippets", allEntries = true)
    public void unstarSnippet(Integer snippetId, User user) {
        Snippet snippet = getById(snippetId);
        snippet.getStarredBy().removeIf(u -> u.getId().equals(user.getId()));
        snippetRepository.save(snippet);
    }

    public List<Snippet> getStarredByUser(User user) {
        return snippetRepository.findStarredByUser(user).stream()
                .filter(snippet -> SnippetAccessPolicy.canRead(snippet, user))
                .toList();
    }
}

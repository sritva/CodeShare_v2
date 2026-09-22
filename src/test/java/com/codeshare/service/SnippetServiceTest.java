package com.codeshare.service;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import com.codeshare.repository.SnippetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SnippetServiceTest {

    @Mock
    private SnippetRepository snippetRepository;

    @Mock
    private jakarta.persistence.EntityManager entityManager;

    @InjectMocks
    private SnippetService snippetService;

    private User owner;
    private User otherUser;
    private Snippet existingSnippet;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1);
        owner.setUsername("michael");

        otherUser = new User();
        otherUser.setId(2);
        otherUser.setUsername("britney");

        existingSnippet = new Snippet();
        existingSnippet.setId(10);
        existingSnippet.setTitle("Hello World");
        existingSnippet.setCode("System.out.println(\"Hello\");");
        existingSnippet.setLanguage("java");
        existingSnippet.setUser(owner);

        when(snippetRepository.findById(10)).thenReturn(Optional.of(existingSnippet));
        when(snippetRepository.findForUpdate(10)).thenReturn(Optional.of(existingSnippet));
    }

    // ── Create ───────────────────────────────────────────────────────────────────

    @Test
    void create_success_returnsSnippet() {
        when(snippetRepository.save(any(Snippet.class))).thenAnswer(i -> i.getArgument(0));
        Snippet input = new Snippet();
        input.setTitle("My Snippet");
        input.setCode("int x = 1;");
        input.setLanguage("java");

        Snippet result = snippetService.create(input, owner);

        assertNotNull(result);
        assertEquals(owner, result.getUser());
        verify(snippetRepository).save(input);
    }

    @Test
    void create_blankTitle_throws() {
        Snippet input = new Snippet();
        input.setTitle("   ");
        input.setCode("int x = 1;");
        input.setLanguage("java");

        assertThrows(IllegalArgumentException.class,
                () -> snippetService.create(input, owner));
    }

    @Test
    void create_blankCode_throws() {
        Snippet input = new Snippet();
        input.setTitle("My Snippet");
        input.setCode("");
        input.setLanguage("java");

        assertThrows(IllegalArgumentException.class,
                () -> snippetService.create(input, owner));
    }

    // ── Update ───────────────────────────────────────────────────────────────────

    @Test
    void update_success_updatesFields() {
        when(snippetRepository.save(any(Snippet.class))).thenAnswer(i -> i.getArgument(0));

        Snippet details = new Snippet();
        details.setTitle("Updated Title");
        details.setCode("int y = 2;");
        details.setLanguage("java");
        details.setPublic(true);
        existingSnippet.setAiExplanation("Old explanation");

        Snippet result = snippetService.update(10, details, owner);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("int y = 2;", result.getCode());
        assertNull(result.getAiExplanation());
    }

    @Test
    void update_languageChange_invalidatesExplanation() {
        existingSnippet.setAiExplanation("Old explanation");
        Snippet details = new Snippet();
        details.setTitle(existingSnippet.getTitle());
        details.setCode(existingSnippet.getCode());
        details.setLanguage("javascript");
        snippetService.update(10, details, owner);
        assertNull(existingSnippet.getAiExplanation());
    }

    @Test
    void update_titleOnly_preservesExplanation() {
        existingSnippet.setAiExplanation("Still valid");
        Snippet details = new Snippet();
        details.setTitle("Renamed");
        details.setCode(existingSnippet.getCode());
        details.setLanguage(existingSnippet.getLanguage());
        snippetService.update(10, details, owner);
        assertEquals("Still valid", existingSnippet.getAiExplanation());
    }

    @Test
    void explanationForOldCode_isRejected() {
        var error = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> snippetService.updateExplanation(10, "outdated code", "java", "Stale result"));
        assertEquals(409, error.getStatusCode().value());
        verify(snippetRepository, never()).save(any());
    }

    @Test
    void explanationForOldLanguage_isRejected() {
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> snippetService.updateExplanation(10, existingSnippet.getCode(), "python", "Stale result"));
        verify(snippetRepository, never()).save(any());
    }

    @Test
    void explanationForCurrentCode_isSaved() {
        snippetService.updateExplanation(10, existingSnippet.getCode(), "java", "Current result");
        assertEquals("Current result", existingSnippet.getAiExplanation());
        verify(snippetRepository).save(existingSnippet);
    }

    @Test
    void update_wrongOwner_throws() {
        Snippet details = new Snippet();
        details.setTitle("Hacked Title");
        details.setCode("malicious();");
        details.setLanguage("java");

        assertThrows(AccessDeniedException.class,
                () -> snippetService.update(10, details, otherUser));
    }

    // ── Delete ───────────────────────────────────────────────────────────────────

    @Test
    void delete_success_callsRepositoryDelete() {
        snippetService.delete(10, owner);

        verify(snippetRepository).delete(existingSnippet);
    }

    @Test
    void delete_wrongOwner_throws() {
        assertThrows(AccessDeniedException.class,
                () -> snippetService.delete(10, otherUser));

        verify(snippetRepository, never()).delete(any());
    }
}

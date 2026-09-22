package com.codeshare.controller;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import com.codeshare.repository.SnippetRepository;
import com.codeshare.repository.UserRepository;
import com.codeshare.service.GeminiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SnippetAuthorizationTest {
    @Autowired MockMvc mvc;
    @Autowired SnippetRepository snippets;
    @Autowired UserRepository users;
    @Autowired jakarta.persistence.EntityManager entityManager;
    @MockBean GeminiClient gemini;
    private Snippet snippet;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setUsername("privacy_owner");
        owner.setPasswordHash("test-hash");
        users.save(owner);
        User other = new User();
        other.setUsername("privacy_other");
        other.setPasswordHash("test-hash");
        users.save(other);
        snippet = new Snippet();
        snippet.setTitle("Private example");
        snippet.setCode("private content");
        snippet.setLanguage("java");
        snippet.setPublic(false);
        snippet.setUser(owner);
        snippets.saveAndFlush(snippet);
    }

    @Test
    void nonOwnerCannotReadOrInteractWithPrivateSnippet() throws Exception {
        String path = "/api/snippets/" + snippet.getId();
        mvc.perform(get(path).with(user("privacy_other"))).andExpect(status().isForbidden());
        for (String action : new String[]{"star", "like", "fork", "explain"}) {
            mvc.perform(post(path + "/" + action).with(user("privacy_other")))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(get(path + "/comments").with(user("privacy_other")))
                .andExpect(status().isForbidden());
        mvc.perform(post(path + "/comments").with(user("privacy_other"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Intrusion\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/snippets/starred").with(user("privacy_other")))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        verifyNoInteractions(gemini);
    }

    @Test
    void ownerCanReadStarAndCommentOnPrivateSnippet() throws Exception {
        String path = "/api/snippets/" + snippet.getId();
        mvc.perform(get(path).with(user("privacy_owner")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("private content"));
        mvc.perform(post(path + "/star").with(user("privacy_owner"))).andExpect(status().isOk());
        mvc.perform(post(path + "/comments").with(user("privacy_owner"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Owner note\"}"))
                .andExpect(status().isCreated());
        mvc.perform(get(path + "/comments").with(user("privacy_owner")))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/snippets/starred").with(user("privacy_owner")))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void previouslyStarredPublicSnippetDisappearsAfterBecomingPrivate() throws Exception {
        snippet.setPublic(true);
        snippets.saveAndFlush(snippet);
        String path = "/api/snippets/" + snippet.getId();
        mvc.perform(post(path + "/star").with(user("privacy_other"))).andExpect(status().isOk());
        mvc.perform(get("/api/snippets/starred").with(user("privacy_other")))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(put(path).with(user("privacy_owner")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Now private\",\"code\":\"private content\",\"language\":\"java\",\"public\":false}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/snippets/starred").with(user("privacy_other")))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        // Removing one's old bookmark remains allowed without exposing the content.
        mvc.perform(post(path + "/unstar").with(user("privacy_other"))).andExpect(status().isOk());
    }

    @Test
    void sharedPrivateForkRequiresTheActualToken() throws Exception {
        snippet.setShareEnabled(true);
        snippet.setShareToken("secret-sharing-token");
        snippets.saveAndFlush(snippet);
        String path = "/api/snippets/" + snippet.getId() + "/fork";
        mvc.perform(post(path).with(user("privacy_other"))).andExpect(status().isForbidden());
        mvc.perform(post(path).with(user("privacy_other")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"shareToken\":\"wrong\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post(path).with(user("privacy_other")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"shareToken\":\"secret-sharing-token\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/snippets/" + snippet.getId() + "/unshare").with(user("privacy_owner")))
                .andExpect(status().isOk());
        mvc.perform(post(path).with(user("privacy_other")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"shareToken\":\"secret-sharing-token\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicDetailDoesNotDiscloseOwnerShareToken() throws Exception {
        snippet.setPublic(true);
        snippet.setShareEnabled(true);
        snippet.setShareToken("owner-only-token");
        snippets.saveAndFlush(snippet);
        String path = "/api/snippets/" + snippet.getId();
        mvc.perform(get(path).with(user("privacy_other")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.shareToken").value(""));
        mvc.perform(get(path).with(user("privacy_owner")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.shareToken").value("owner-only-token"));
        mvc.perform(get("/api/users/privacy_owner"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.snippets[0].shareToken").doesNotExist());
    }

    @Test
    void anonymousPublicReadsWorkButPrivateReadsAndMutationsDoNot() throws Exception {
        String path = "/api/snippets/" + snippet.getId();
        mvc.perform(get(path)).andExpect(status().isForbidden());
        mvc.perform(get(path + "/comments")).andExpect(status().isForbidden());
        snippet.setPublic(true);
        snippets.saveAndFlush(snippet);
        mvc.perform(get(path)).andExpect(status().isOk());
        mvc.perform(get(path + "/comments")).andExpect(status().isOk());
        mvc.perform(get("/api/users/privacy_owner")).andExpect(status().isOk());
        mvc.perform(get("/user/privacy_owner")).andExpect(forwardedUrl("/index.html"));
        mvc.perform(get("/starred")).andExpect(forwardedUrl("/index.html"));
        mvc.perform(get("/api/snippets/starred")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/snippets/mine")).andExpect(status().isUnauthorized());
        mvc.perform(post(path + "/star")).andExpect(status().isUnauthorized());
        mvc.perform(post(path + "/explain")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/users/profile").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void frontendIsPublicFieldCreatesAndUpdatesPrivateSnippets() throws Exception {
        mvc.perform(post("/api/snippets").with(user("privacy_owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Private via UI\",\"code\":\"secret\",\"language\":\"java\",\"isPublic\":false}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.public").value(false));
        snippet.setPublic(true);
        snippets.saveAndFlush(snippet);
        mvc.perform(put("/api/snippets/" + snippet.getId()).with(user("privacy_owner"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Private via UI\",\"code\":\"secret\",\"language\":\"java\",\"isPublic\":false}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/snippets/" + snippet.getId()).with(user("privacy_other")))
                .andExpect(status().isForbidden());
    }

    @Test
    void explanationDoesNotOverwriteContentChangedDuringInference() throws Exception {
        when(gemini.generateExplanation("private content", "java")).thenAnswer(invocation -> {
            // Simulate a database revision arriving while the model call is in progress.
            // The request still holds the old entity in its first-level persistence cache.
            entityManager.createQuery("update Snippet s set s.code = :code where s.id = :id")
                    .setParameter("code", "new revision").setParameter("id", snippet.getId())
                    .executeUpdate();
            return "Explanation of the old revision";
        });
        mvc.perform(post("/api/snippets/" + snippet.getId() + "/explain").with(user("privacy_owner")))
                .andExpect(status().isConflict());
        entityManager.refresh(snippet);
        assertEquals("new revision", snippet.getCode());
        assertNull(snippet.getAiExplanation());
    }
}

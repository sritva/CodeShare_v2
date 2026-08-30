package com.codeshare.repository;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SnippetQueryPerformanceTest {

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private SessionFactory sessionFactory;
    private Statistics statistics;

    @BeforeEach
    void setUp() {
        sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        snippetRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    void findByIsPublicTrue_preventsNPlusOneQueriesOnAssociations() {
        // Setup users
        User author = new User();
        author.setUsername("author_user");
        author.setPasswordHash("hash123");
        userRepository.save(author);

        User liker = new User();
        liker.setUsername("liker_user");
        liker.setPasswordHash("hash123");
        userRepository.save(liker);

        User starrer = new User();
        starrer.setUsername("starrer_user");
        starrer.setPasswordHash("hash123");
        userRepository.save(starrer);

        // Setup parent snippet (with likes and stars)
        Snippet parent = new Snippet();
        parent.setTitle("Parent Snippet");
        parent.setCode("public class Parent {}");
        parent.setLanguage("java");
        parent.setPublic(true);
        parent.setUser(author);
        parent.getLikedBy().add(liker);
        parent.getStarredBy().add(starrer);
        snippetRepository.save(parent);

        // Setup 5 child snippets that have likes, stars, and parent references
        for (int i = 1; i <= 5; i++) {
            Snippet child = new Snippet();
            child.setTitle("Snippet " + i);
            child.setCode("System.out.println(" + i + ");");
            child.setLanguage("java");
            child.setPublic(true);
            child.setUser(author);
            child.setParent(parent);
            child.getLikedBy().add(liker);
            child.getStarredBy().add(starrer);
            snippetRepository.save(child);
        }

        // Flush and detach everything so queries are forced to hit the database
        entityManager.flush();
        entityManager.clear();

        statistics.clear();

        // Execute paginated public query
        Page<Snippet> snippetPage = snippetRepository.findByIsPublicTrue(PageRequest.of(0, 10));

        // Traverse all associated properties accessed by SnippetApiController feed mapping
        for (Snippet snippet : snippetPage.getContent()) {
            assertNotNull(snippet.getUsername());
            assertEquals("author_user", snippet.getUsername());
            assertFalse(snippet.getLikedBy().isEmpty());
            assertTrue(snippet.getLikedBy().stream().anyMatch(u -> u.getUsername().equals("liker_user")));
            assertFalse(snippet.getStarredBy().isEmpty());
            assertTrue(snippet.getStarredBy().stream().anyMatch(u -> u.getUsername().equals("starrer_user")));
            if (snippet.getParent() != null) {
                assertEquals("Parent Snippet", snippet.getParentTitle());
                assertEquals("author_user", snippet.getParentUsername());
                assertNotNull(snippet.getParentIdValue());
            }
        }

        long statementCount = statistics.getPrepareStatementCount();

        // 1 query for snippet entity graph with joins + 1 count query for Spring Data Page = 2 queries total.
        // If N+1 queries were present, statementCount would be > 20.
        assertTrue(statementCount <= 2, "Expected at most 2 SQL statements for paginated fetch, but got: " + statementCount);
    }

    @Test
    @Transactional
    void search_preventsNPlusOneQueriesOnAssociations() {
        User author = new User();
        author.setUsername("search_author");
        author.setPasswordHash("hash123");
        userRepository.save(author);

        User fan = new User();
        fan.setUsername("search_fan");
        fan.setPasswordHash("hash123");
        userRepository.save(fan);

        Snippet parent = new Snippet();
        parent.setTitle("Base Algorithm");
        parent.setCode("void algorithm() {}");
        parent.setLanguage("python");
        parent.setPublic(true);
        parent.setUser(author);
        parent.getLikedBy().add(fan);
        parent.getStarredBy().add(fan);
        snippetRepository.save(parent);

        for (int i = 1; i <= 5; i++) {
            Snippet child = new Snippet();
            child.setTitle("QuickSort Search Variant " + i);
            child.setCode("def quicksort_" + i + "(): pass");
            child.setLanguage("python");
            child.setPublic(true);
            child.setUser(author);
            child.setParent(parent);
            child.getLikedBy().add(fan);
            child.getStarredBy().add(fan);
            snippetRepository.save(child);
        }

        entityManager.flush();
        entityManager.clear();

        statistics.clear();

        Page<Snippet> searchResults = snippetRepository.findByIsPublicTrueAndTitleContainingIgnoreCaseAndLanguageIgnoreCase(
                "QuickSort", "python", PageRequest.of(0, 10));

        for (Snippet snippet : searchResults.getContent()) {
            assertNotNull(snippet.getUsername());
            assertEquals("search_author", snippet.getUsername());
            assertFalse(snippet.getLikedBy().isEmpty());
            assertFalse(snippet.getStarredBy().isEmpty());
            assertEquals("Base Algorithm", snippet.getParentTitle());
            assertEquals("search_author", snippet.getParentUsername());
        }

        long statementCount = statistics.getPrepareStatementCount();
        assertTrue(statementCount <= 2, "Expected at most 2 SQL statements for search query, but got: " + statementCount);
    }
}

package com.codeshare.repository;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SnippetRepository extends JpaRepository<Snippet, Integer> {

    // Find public snippets ordered by date (full list — used by getByUser, etc.)
    List<Snippet> findByIsPublicTrueOrderByCreatedAtDesc();

    // Paginated public snippets
    Page<Snippet> findByIsPublicTrue(Pageable pageable);

    // Find snippets by user
    List<Snippet> findByUserOrderByCreatedAtDesc(User user);

    // Search by title (only searching public snippets is standard)
    List<Snippet> findByIsPublicTrueAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);

    // Paginated search by title
    Page<Snippet> findByIsPublicTrueAndTitleContainingIgnoreCase(String title, Pageable pageable);

    // Search by language
    List<Snippet> findByIsPublicTrueAndLanguageIgnoreCaseOrderByCreatedAtDesc(String language);

    // Paginated search by language
    Page<Snippet> findByIsPublicTrueAndLanguageIgnoreCase(String language, Pageable pageable);

    // Search by title and language
    List<Snippet> findByIsPublicTrueAndTitleContainingIgnoreCaseAndLanguageIgnoreCaseOrderByCreatedAtDesc(String title, String language);

    // Paginated search by title and language
    Page<Snippet> findByIsPublicTrueAndTitleContainingIgnoreCaseAndLanguageIgnoreCase(String title, String language, Pageable pageable);
}

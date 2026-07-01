package com.codeshare.repository;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SnippetRepository extends JpaRepository<Snippet, Integer> {

    List<Snippet> findByIsPublicTrueOrderByCreatedAtDesc();

    Page<Snippet> findByIsPublicTrue(Pageable pageable);

    List<Snippet> findByUserOrderByCreatedAtDesc(User user);

    List<Snippet> findByIsPublicTrueAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);

    Page<Snippet> findByIsPublicTrueAndTitleContainingIgnoreCase(String title, Pageable pageable);

    List<Snippet> findByIsPublicTrueAndLanguageIgnoreCaseOrderByCreatedAtDesc(String language);

    Page<Snippet> findByIsPublicTrueAndLanguageIgnoreCase(String language, Pageable pageable);

    List<Snippet> findByIsPublicTrueAndTitleContainingIgnoreCaseAndLanguageIgnoreCaseOrderByCreatedAtDesc(String title, String language);

    Page<Snippet> findByIsPublicTrueAndTitleContainingIgnoreCaseAndLanguageIgnoreCase(String title, String language, Pageable pageable);

    Optional<Snippet> findByShareToken(String shareToken);
}

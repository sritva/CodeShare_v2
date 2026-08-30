package com.codeshare.repository;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SnippetRepository extends JpaRepository<Snippet, Integer> {

    @EntityGraph(attributePaths = {"user", "likedBy", "starredBy", "parent", "parent.user"})
    List<Snippet> findByIsPublicTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "likedBy", "starredBy", "parent", "parent.user"})
    Page<Snippet> findByIsPublicTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "likedBy", "starredBy", "parent", "parent.user"})
    List<Snippet> findByUserOrderByCreatedAtDesc(User user);

    @EntityGraph(attributePaths = {"user", "likedBy", "starredBy", "parent", "parent.user"})
    List<Snippet> findByUserAndIsPublicTrueOrderByCreatedAtDesc(User user);

    @EntityGraph(attributePaths = {"user", "likedBy", "starredBy", "parent", "parent.user"})
    List<Snippet> findByIsPublicTrueAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title);

    @EntityGraph(attributePaths = {"user", "likedBy", "starredBy", "parent", "parent.user"})
    Page<Snippet> findByIsPublicTrueAndTitleContainingIgnoreCase(String title, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "likedBy", "starredBy", "parent", "parent.user"})
    List<Snippet> findByIsPublicTrueAndLanguageIgnoreCaseOrderByCreatedAtDesc(String language);

    @EntityGraph(attributePaths = {"user", "likedBy", "starredBy", "parent", "parent.user"})
    Page<Snippet> findByIsPublicTrueAndLanguageIgnoreCase(String language, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "likedBy", "starredBy", "parent", "parent.user"})
    List<Snippet> findByIsPublicTrueAndTitleContainingIgnoreCaseAndLanguageIgnoreCaseOrderByCreatedAtDesc(String title, String language);

    @EntityGraph(attributePaths = {"user", "likedBy", "starredBy", "parent", "parent.user"})
    Page<Snippet> findByIsPublicTrueAndTitleContainingIgnoreCaseAndLanguageIgnoreCase(String title, String language, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "likedBy", "starredBy", "parent", "parent.user"})
    @org.springframework.data.jpa.repository.Query("select s from Snippet s join s.starredBy u where u = :user order by s.createdAt desc")
    List<Snippet> findStarredByUser(@org.springframework.data.repository.query.Param("user") User user);

    @EntityGraph(attributePaths = {"user", "likedBy", "starredBy", "parent", "parent.user"})
    Optional<Snippet> findByShareToken(String shareToken);
}

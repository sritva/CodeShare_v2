package com.codeshare.repository;

import com.codeshare.model.Comment;
import com.codeshare.model.Snippet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @EntityGraph(attributePaths = {"user"})
    List<Comment> findBySnippetOrderByCreatedAtAsc(Snippet snippet);
}

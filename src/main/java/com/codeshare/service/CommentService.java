package com.codeshare.service;

import com.codeshare.model.Comment;
import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import com.codeshare.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> getCommentsForSnippet(Snippet snippet) {
        return commentRepository.findBySnippetOrderByCreatedAtAsc(snippet);
    }

    @Transactional
    public Comment addComment(Snippet snippet, User user, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        Comment comment = new Comment();
        comment.setSnippet(snippet);
        comment.setUser(user);
        comment.setContent(content.trim());
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Integer commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Comment not found"));
        
        // Ownership check: only comment owner OR snippet owner can delete
        boolean isCommentOwner = comment.getUser().getId().equals(user.getId());
        boolean isSnippetOwner = comment.getSnippet().getUser().getId().equals(user.getId());

        if (!isCommentOwner && !isSnippetOwner) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }
}

package com.codeshare.service;

import com.codeshare.model.Comment;
import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import com.codeshare.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    public void addComment_exceedsLength_throwsException() {
        Snippet snippet = new Snippet();
        snippet.setPublic(true);
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        
        String longContent = "a".repeat(1001);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            commentService.addComment(snippet, user, longContent);
        });
        
        assertEquals("Comment must be under 1000 characters", ex.getMessage());
    }
}

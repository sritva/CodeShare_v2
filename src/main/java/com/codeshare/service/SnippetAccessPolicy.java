package com.codeshare.service;

import com.codeshare.model.Snippet;
import com.codeshare.model.User;
import org.springframework.security.access.AccessDeniedException;

/** One visibility policy for ID-based access. Sharing requires possession of a token. */
public final class SnippetAccessPolicy {
    private SnippetAccessPolicy() {}

    public static boolean isOwner(Snippet snippet, User user) {
        return user != null && user.getId() != null && snippet.getUser() != null
                && user.getId().equals(snippet.getUser().getId());
    }

    public static boolean canRead(Snippet snippet, User user) {
        return snippet.isPublic() || isOwner(snippet, user);
    }

    public static void requireRead(Snippet snippet, User user) {
        if (!canRead(snippet, user)) {
            throw new AccessDeniedException("You do not have permission to access this snippet");
        }
    }

    public static void requireInteraction(Snippet snippet, User user) {
        if (user == null) {
            throw new AccessDeniedException("Authentication is required");
        }
        requireRead(snippet, user);
    }

    public static void requireFork(Snippet snippet, User user, String shareToken) {
        if (user == null) {
            throw new AccessDeniedException("Authentication is required");
        }
        boolean validShare = snippet.isShareEnabled() && snippet.getShareToken() != null
                && snippet.getShareToken().equals(shareToken);
        if (!canRead(snippet, user) && !validShare) {
            throw new AccessDeniedException("A valid share token is required to fork this private snippet");
        }
    }
}

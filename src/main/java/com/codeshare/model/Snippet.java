package com.codeshare.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "snippets", indexes = {
    @Index(name = "idx_snippets_share_token", columnList = "share_token")
})
public class Snippet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Code cannot be blank")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    @NotBlank(message = "Language cannot be blank")
    @Size(max = 50, message = "Language cannot exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String language = "text";

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(name = "ai_explanation", columnDefinition = "TEXT")
    private String aiExplanation;

    @Column(name = "share_token", unique = true, length = 36)
    private String shareToken;

    @Column(name = "share_enabled", nullable = false)
    private boolean shareEnabled = false;

    @Column(name = "shared_at")
    private LocalDateTime sharedAt;

    public Snippet() {
    }

    public Snippet(Integer id, String title, String code, String language, boolean isPublic, LocalDateTime createdAt, User user) {
        this.id = id;
        this.title = title;
        this.code = code;
        this.language = language;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }

    public String getUsername() {
        return user != null ? user.getUsername() : "";
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public boolean isShareEnabled() {
        return shareEnabled;
    }

    public void setShareEnabled(boolean shareEnabled) {
        this.shareEnabled = shareEnabled;
    }

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(LocalDateTime sharedAt) {
        this.sharedAt = sharedAt;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "snippet_likes",
        joinColumns = @JoinColumn(name = "snippet_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.Set<User> likedBy = new java.util.HashSet<>();

    public java.util.Set<User> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(java.util.Set<User> likedBy) {
        this.likedBy = likedBy;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "snippet_stars",
        joinColumns = @JoinColumn(name = "snippet_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.Set<User> starredBy = new java.util.HashSet<>();

    public java.util.Set<User> getStarredBy() {
        return starredBy;
    }

    public void setStarredBy(java.util.Set<User> starredBy) {
        this.starredBy = starredBy;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Snippet parent;

    public Snippet getParent() {
        return parent;
    }

    public void setParent(Snippet parent) {
        this.parent = parent;
    }

    public String getParentUsername() {
        return parent != null ? parent.getUsername() : null;
    }

    public String getParentTitle() {
        return parent != null ? parent.getTitle() : null;
    }

    public Integer getParentIdValue() {
        return parent != null ? parent.getId() : null;
    }
}

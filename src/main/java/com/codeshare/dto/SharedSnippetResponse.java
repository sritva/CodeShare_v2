package com.codeshare.dto;

import java.time.LocalDateTime;

public class SharedSnippetResponse {
    private Integer id;
    private String title;
    private String code;
    private String language;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String aiExplanation;
    private Integer parentId;
    private String parentTitle;
    private String parentUsername;

    public SharedSnippetResponse(Integer id, String title, String code,
            String language, String username,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            String aiExplanation, Integer parentId,
            String parentTitle, String parentUsername) {
        this.id = id;
        this.title = title;
        this.code = code;
        this.language = language;
        this.username = username;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.aiExplanation = aiExplanation;
        this.parentId = parentId;
        this.parentTitle = parentTitle;
        this.parentUsername = parentUsername;
    }

    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getCode() { return code; }
    public String getLanguage() { return language; }
    public String getUsername() { return username; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getAiExplanation() { return aiExplanation; }
    public Integer getParentId() { return parentId; }
    public String getParentTitle() { return parentTitle; }
    public String getParentUsername() { return parentUsername; }
}

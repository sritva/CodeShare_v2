package com.codeshare.dto;

import java.time.LocalDateTime;

public class SharedSnippetResponse {
    private String title;
    private String code;
    private String language;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String aiExplanation;

    public SharedSnippetResponse(String title, String code,
            String language, String username,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            String aiExplanation) {
        this.title = title;
        this.code = code;
        this.language = language;
        this.username = username;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.aiExplanation = aiExplanation;
    }

    public String getTitle() { return title; }
    public String getCode() { return code; }
    public String getLanguage() { return language; }
    public String getUsername() { return username; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getAiExplanation() { return aiExplanation; }
}

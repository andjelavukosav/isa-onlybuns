package com.example.agency;

public class AdPostMessage {
    private String description;
    private String createdAt;
    private String username;

    public AdPostMessage() {}

    public AdPostMessage(String description, String createdAt, String username) {
        this.description = description;
        this.createdAt = createdAt;
        this.username = username;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

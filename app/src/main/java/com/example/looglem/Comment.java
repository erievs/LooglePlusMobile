package com.example.looglem;

public class Comment {
    private final String username;
    private final String content;

    public Comment(String username, String content) {
        this.username = username;
        this.content = content;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }
}



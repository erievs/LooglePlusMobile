package com.example.looglem;

import java.util.List;

public class Post {
    private final String id;
    private final String username;
    private final String content;
    private final String imageUrl;
    private final String createdAt;
    private final String postLinkUrl;
    private final List<Comment> comments;

    public Post(String id, String username, String content, String imageUrl, String createdAt, String postLinkUrl, List<Comment> comments) {
        this.id = id;
        this.username = username;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.postLinkUrl = postLinkUrl;
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPostLinkUrl() {
        return postLinkUrl;
    }

    public List<Comment> getComments() {
        return comments;
    }
}

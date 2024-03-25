package com.example.looglem;

import java.util.List;

public class Post {
    private final String id;
    private final String username;
    private final String content;
    private final String image_url;
    private final String createdAt;
    private final List<Comment> comments;

    public Post(String id, String username, String content, String image_url, String createdAt, List<Comment> comments) {
        this.id = id;
        this.username = username;
        this.content = content;
        this.image_url = image_url;
        this.createdAt = createdAt;
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
        return image_url;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<Comment> getComments() {
        return comments;
    }
}

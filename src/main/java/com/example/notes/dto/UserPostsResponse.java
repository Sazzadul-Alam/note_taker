package com.example.notes.dto;

import com.example.notes.model.Post;
import java.util.List;

public class UserPostsResponse {
    private String userId;
    private String userName;
    private String email;
    private List<Post> posts;
    private long total;
    private int page;
    private int size;

    public UserPostsResponse() {
    }

    public UserPostsResponse(String userId, String userName, String email, List<Post> posts, long total, int page, int size) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.posts = posts;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}


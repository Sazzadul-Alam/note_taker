package com.example.notes.controller;

import com.example.notes.dto.PostRequests.CreatePostRequest;
import com.example.notes.model.Post;
import com.example.notes.repository.PostRepository;
import com.example.notes.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // POST /api/posts creates a post for the caller. Posts are visible to everyone.
    @PostMapping
    public ResponseEntity<Post> createPost(@AuthenticationPrincipal UserPrincipal currentUser,
                                           @Valid @RequestBody CreatePostRequest request) {
        Post post = new Post(currentUser.getId(), request.getTitle(), request.getContent());
        Post saved = postRepository.save(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}


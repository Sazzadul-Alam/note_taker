package com.example.notes.controller;

import com.example.notes.dto.AdminUserRequests.CreateUserRequest;
import com.example.notes.dto.AdminUserRequests.UpdateUserRequest;
import com.example.notes.dto.PageResponse;
import com.example.notes.model.Note;
import com.example.notes.model.User;
import com.example.notes.repository.NoteRepository;
import com.example.notes.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository,
                           NoteRepository noteRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Admin Notes: GET /api/admin/notes?page=&size=
    // Uses default _id index: sorted by _id desc
    @GetMapping("/notes")
    public ResponseEntity<PageResponse<Note>> listAllNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Note> notePage = noteRepository.findAll(pageable);
        return ResponseEntity.ok(new PageResponse<>(
                notePage.getContent(),
                notePage.getTotalElements(),
                notePage.getNumber(),
                notePage.getSize()
        ));
    }

    // Admin Users: POST /api/admin/users
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        String role = (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) ? "ADMIN" : "USER";
        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                role,
                request.getInterests()
        );

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Admin Users: PUT /api/admin/users/{id}
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            // Check email uniqueness if changed
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole().equalsIgnoreCase("ADMIN") ? "ADMIN" : "USER");
        }
        if (request.getInterests() != null) {
            user.setInterests(request.getInterests());
        }

        User updated = userRepository.save(user);
        return ResponseEntity.ok(updated);
    }

    // Admin Users: DELETE /api/admin/users/{id}
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Admin Users: GET /api/admin/users/{id}
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin Users: GET /api/admin/users?page=&size=
    // Uses default _id index: sorted by _id desc
    @GetMapping("/users")
    public ResponseEntity<PageResponse<User>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> userPage = userRepository.findAll(pageable);
        return ResponseEntity.ok(new PageResponse<>(
                userPage.getContent(),
                userPage.getTotalElements(),
                userPage.getNumber(),
                userPage.getSize()
        ));
    }
}


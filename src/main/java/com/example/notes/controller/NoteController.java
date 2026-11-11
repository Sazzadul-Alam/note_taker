package com.example.notes.controller;

import com.example.notes.dto.NoteRequests.CreateNoteRequest;
import com.example.notes.dto.NoteRequests.UpdateNoteRequest;
import com.example.notes.dto.PageResponse;
import com.example.notes.model.Note;
import com.example.notes.repository.NoteRepository;
import com.example.notes.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteRepository noteRepository;

    public NoteController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @PostMapping
    public ResponseEntity<Note> createNote(@AuthenticationPrincipal UserPrincipal currentUser,
                                           @Valid @RequestBody CreateNoteRequest request) {
        Note note = new Note(currentUser.getId(), request.getTitle(), request.getContent());
        Note saved = noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@AuthenticationPrincipal UserPrincipal currentUser,
                                           @PathVariable String id,
                                           @RequestBody UpdateNoteRequest request) {
        Note note = noteRepository.findByIdAndOwnerId(id, currentUser.getId()).orElse(null);
        if (note == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getTitle() != null) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }

        Note updated = noteRepository.save(note);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@AuthenticationPrincipal UserPrincipal currentUser,
                                           @PathVariable String id) {
        Note note = noteRepository.findByIdAndOwnerId(id, currentUser.getId()).orElse(null);
        if (note == null) {
            return ResponseEntity.notFound().build();
        }

        noteRepository.delete(note);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@AuthenticationPrincipal UserPrincipal currentUser,
                                            @PathVariable String id) {
        return noteRepository.findByIdAndOwnerId(id, currentUser.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<PageResponse<Note>> listNotes(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Matching index: { ownerId: 1, _id: -1 }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Note> notePage = noteRepository.findByOwnerId(currentUser.getId(), pageable);

        return ResponseEntity.ok(new PageResponse<>(
                notePage.getContent(),
                notePage.getTotalElements(),
                notePage.getNumber(),
                notePage.getSize()
        ));
    }
}


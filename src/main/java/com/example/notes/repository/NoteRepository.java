package com.example.notes.repository;

import com.example.notes.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
    Page<Note> findByOwnerId(String ownerId, Pageable pageable);
    Optional<Note> findByIdAndOwnerId(String id, String ownerId);
    void deleteByIdAndOwnerId(String id, String ownerId);
}


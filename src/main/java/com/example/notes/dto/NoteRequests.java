package com.example.notes.dto;

import jakarta.validation.constraints.NotBlank;

public class NoteRequests {

    public static class CreateNoteRequest {
        @NotBlank
        private String title;

        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class UpdateNoteRequest {
        private String title;
        private String content;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}


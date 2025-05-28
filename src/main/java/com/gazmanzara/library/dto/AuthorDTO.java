package com.gazmanzara.library.dto;

import com.gazmanzara.library.model.Author;

public class AuthorDTO {
    public Long id;
    public String name;

    public AuthorDTO(Author author) {
        this.id = author.getId();
        this.name = author.getName();
    }
}

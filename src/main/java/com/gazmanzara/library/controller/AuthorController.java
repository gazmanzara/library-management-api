package com.gazmanzara.library.controller;

import com.gazmanzara.library.model.Author;
import com.gazmanzara.library.repository.AuthorRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/authors")
public class AuthorController {

    @Autowired
    private AuthorRepository authorRepository;

    // Get all authors
    @GetMapping
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    // Create new author
    @PostMapping
    public ResponseEntity<?> createAuthor(@Valid @RequestBody Author author) {
        if (authorRepository.existsByName(author.getName())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("msg", "Author name exist"));
        }
        Author savedAuthor = authorRepository.save(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAuthor);
    }

    // Get author by ID
    @GetMapping("/{id}")
    public Author getAuthorById(@PathVariable Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id " + id));
    }

    // Delete author by ID
    @DeleteMapping("/{id}")
    public void deleteAuthor(@PathVariable Long id) {
        authorRepository.deleteById(id);
    }
}

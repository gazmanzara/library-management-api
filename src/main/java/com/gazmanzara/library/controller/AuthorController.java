package com.gazmanzara.library.controller;

import com.gazmanzara.library.model.Author;
import com.gazmanzara.library.repository.AuthorRepository;
import com.gazmanzara.library.exception.ResourceNotFoundException;
import com.gazmanzara.library.exception.ResourceAlreadyExistsException;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        return ResponseEntity.ok(authorRepository.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Author>> searchAuthors(@RequestParam String name) {
        return ResponseEntity.ok(authorRepository.findByNameContainingIgnoreCase(name));
    }

    @PostMapping
    public ResponseEntity<Author> createAuthor(@Valid @RequestBody Author author) {
        if (authorRepository.existsByName(author.getName())) {
            throw new ResourceAlreadyExistsException("Author", "name", author.getName());
        }
        
        Author savedAuthor = authorRepository.save(author);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedAuthor.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(savedAuthor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", id));
        return ResponseEntity.ok(author);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author", "id", id);
        }
        authorRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable Long id, @Valid @RequestBody Author authorRequest) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", id));

        // Check if new name is different and already exists
        if (!author.getName().equals(authorRequest.getName()) && authorRepository.existsByName(authorRequest.getName())) {
            throw new ResourceAlreadyExistsException("Author", "name", authorRequest.getName());
        }

        // Update author properties
        author.setName(authorRequest.getName());
        author.setBiography(authorRequest.getBiography());

        Author updatedAuthor = authorRepository.save(author);
        return ResponseEntity.ok(updatedAuthor);
    }
}

package com.gazmanzara.library.controller;

import com.gazmanzara.library.model.Book;
import com.gazmanzara.library.model.Author;
import com.gazmanzara.library.model.Category;
import com.gazmanzara.library.repository.BookRepository;
import com.gazmanzara.library.repository.AuthorRepository;
import com.gazmanzara.library.repository.CategoryRepository;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createBook(@Valid @RequestBody BookRequest bookRequest) {
        Optional<Author> author = authorRepository.findById(bookRequest.getAuthorId());
        if (author.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("msg", "Author not found"));
        }

        List<Category> categories = categoryRepository.findAllById(bookRequest.getCategoryIds());
        if (categories.size() != bookRequest.getCategoryIds().size()) {
            return ResponseEntity.badRequest().body(Map.of("msg", "Some categories not found"));
        }

        Book book = new Book();
        book.setTitle(bookRequest.getTitle());
        book.setDescription(bookRequest.getDescription());
        book.setImgUrl(bookRequest.getImgUrl());
        book.setAuthor(author.get());
        book.setCategories(new HashSet<>(categories));

        return ResponseEntity.ok(bookRepository.save(book));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBook(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body((Book) Map.of("msg", "Book not found")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("msg", "Book not found"));
        }
        bookRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("msg", "Book deleted"));
    }

    // DTO for book creation
    public static class BookRequest {
        private String title;
        private String description;
        private String imgUrl;
        private Long authorId;
        private List<Long> categoryIds;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getImgUrl() { return imgUrl; }
        public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

        public Long getAuthorId() { return authorId; }
        public void setAuthorId(Long authorId) { this.authorId = authorId; }

        public List<Long> getCategoryIds() { return categoryIds; }
        public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }
    }
}

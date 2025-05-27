package com.gazmanzara.library.controller;

import com.gazmanzara.library.model.Book;
import com.gazmanzara.library.model.Author;
import com.gazmanzara.library.model.Category;
import com.gazmanzara.library.repository.BookRepository;
import com.gazmanzara.library.repository.AuthorRepository;
import com.gazmanzara.library.repository.CategoryRepository;
import com.gazmanzara.library.repository.BorrowedBookRepository;
import com.gazmanzara.library.exception.ResourceNotFoundException;
import com.gazmanzara.library.exception.ResourceAlreadyExistsException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public BookController(
            BookRepository bookRepository,
            AuthorRepository authorRepository,
            CategoryRepository categoryRepository,
            BorrowedBookRepository borrowedBookRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Integer year) {

        if (isbn != null) {
            return bookRepository.findByIsbn(isbn)
                    .map(book -> ResponseEntity.ok(List.of(book)))
                    .orElse(ResponseEntity.ok(List.of()));
        }

        if (title != null) {
            return ResponseEntity.ok(bookRepository.findByTitleContainingIgnoreCase(title));
        }

        if (authorId != null) {
            return ResponseEntity.ok(bookRepository.findByAuthorId(authorId));
        }

        if (year != null) {
            return ResponseEntity.ok(bookRepository.findByPublicationYear(year));
        }

        return ResponseEntity.ok(bookRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody BookRequest bookRequest) {
        // Check if ISBN already exists
        if (bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            throw new ResourceAlreadyExistsException("Book", "ISBN", bookRequest.getIsbn());
        }

        // Check if author exists
        Author author = authorRepository.findById(bookRequest.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", bookRequest.getAuthorId()));

        // Check if all categories exist
        List<Category> categories = categoryRepository.findAllById(bookRequest.getCategoryIds());
        if (categories.size() != bookRequest.getCategoryIds().size()) {
            throw new ResourceNotFoundException("Category", "ids", bookRequest.getCategoryIds());
        }

        Book book = new Book(
                bookRequest.getTitle(),
                bookRequest.getIsbn(),
                author);
        book.setDescription(bookRequest.getDescription());
        book.setImgUrl(bookRequest.getImgUrl());
        book.setPublicationYear(bookRequest.getPublicationYear());
        book.setCategories(new HashSet<>(categories));

        Book savedBook = bookRepository.save(book);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedBook.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedBook);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        return ResponseEntity.ok(book);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book", "id", id);
        }
        bookRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest bookRequest) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        // Check if new ISBN is different and already exists
        if (!book.getIsbn().equals(bookRequest.getIsbn()) && bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            throw new ResourceAlreadyExistsException("Book", "ISBN", bookRequest.getIsbn());
        }

        // Check if author exists
        Author author = authorRepository.findById(bookRequest.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", bookRequest.getAuthorId()));

        // Check if all categories exist
        List<Category> categories = categoryRepository.findAllById(bookRequest.getCategoryIds());
        if (categories.size() != bookRequest.getCategoryIds().size()) {
            throw new ResourceNotFoundException("Category", "ids", bookRequest.getCategoryIds());
        }

        // Update book properties
        book.setTitle(bookRequest.getTitle());
        book.setDescription(bookRequest.getDescription());
        book.setIsbn(bookRequest.getIsbn());
        book.setImgUrl(bookRequest.getImgUrl());
        book.setPublicationYear(bookRequest.getPublicationYear());
        book.setAuthor(author);
        book.setCategories(new HashSet<>(categories));

        Book updatedBook = bookRepository.save(book);
        return ResponseEntity.ok(updatedBook);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Book>> getActiveBooks() {
        return ResponseEntity.ok(bookRepository.findActiveBooks());
    }

    public static class BookRequest {
        @NotBlank(message = "Book title must not be blank")
        private String title;

        private String description;

        private String imgUrl;

        @NotBlank(message = "ISBN must not be blank")
        private String isbn;

        private Integer publicationYear;

        @NotNull(message = "Author ID is required")
        private Long authorId;

        @NotEmpty(message = "At least one category is required")
        private List<Long> categoryIds;

        // Getters and setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        public Integer getPublicationYear() {
            return publicationYear;
        }

        public void setPublicationYear(Integer publicationYear) {
            this.publicationYear = publicationYear;
        }

        public Long getAuthorId() {
            return authorId;
        }

        public void setAuthorId(Long authorId) {
            this.authorId = authorId;
        }

        public List<Long> getCategoryIds() {
            return categoryIds;
        }

        public void setCategoryIds(List<Long> categoryIds) {
            this.categoryIds = categoryIds;
        }
    }
}
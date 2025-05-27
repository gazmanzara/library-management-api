package com.gazmanzara.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Book title must not be blank")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imgUrl;

    @NotNull(message = "ISBN must not be null")
    @Column(unique = true)
    private String isbn;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BorrowedBook> borrowedBooks = new HashSet<>();

    public Book() {
    }

    public Book(String title, String isbn, Author author) {
        this.title = title;
        this.isbn = isbn;
        this.author = author;
    }

    // Helper method to check if book is currently borrowed
    @JsonProperty("currentlyBorrowed")
    public boolean isCurrentlyBorrowed() {
        return borrowedBooks.stream()
                .anyMatch(borrow -> borrow.getStatus() == BorrowStatus.BORROWED);
    }

    // Helper method to get current borrow record if exists
    @JsonProperty("currentBorrow")
    public Long getCurrentBorrowId() {
        return borrowedBooks.stream()
                .filter(borrow -> borrow.getStatus() == BorrowStatus.BORROWED)
                .findFirst()
                .map(BorrowedBook::getId)
                .orElse(null);
    }

    // Helper method to add a borrow record
    public void addBorrowedBook(BorrowedBook borrowedBook) {
        borrowedBooks.add(borrowedBook);
        borrowedBook.setBook(this);
    }

    // Helper method to remove a borrow record
    public void removeBorrowedBook(BorrowedBook borrowedBook) {
        borrowedBooks.remove(borrowedBook);
        borrowedBook.setBook(null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    @JsonIgnore
    public Set<BorrowedBook> getBorrowedBooks() {
        return borrowedBooks;
    }

    public void setBorrowedBooks(Set<BorrowedBook> borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
    }
}

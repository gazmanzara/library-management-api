package com.gazmanzara.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name cannot be empty")
    @Column(name = "first_name")
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    @Column(name = "last_name")
    private String lastName;

    @Email(message = "Please provide a valid email address")
    @Column(unique = true)
    private String email;

    @Column(unique = true)
    @NotNull(message = "Phone is required")
    private String phone;

    @JsonIgnore
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BorrowedBook> borrowedBooks = new HashSet<>();

    public Member() {
    }

    public Member(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @JsonIgnore
    public Set<BorrowedBook> getBorrowedBooks() {
        return borrowedBooks;
    }

    public void setBorrowedBooks(Set<BorrowedBook> borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
    }

    @JsonProperty("currentBorrows")
    public Set<Long> getCurrentBorrowIds() {
        return getCurrentBorrows().stream()
                .map(BorrowedBook::getId)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<BorrowedBook> getCurrentBorrows() {
        return borrowedBooks.stream()
                .filter(borrow -> borrow.getStatus() == BorrowStatus.BORROWED)
                .collect(Collectors.toSet());
    }

    public boolean hasOverdueBooks() {
        return getCurrentBorrows().stream()
                .anyMatch(borrow -> borrow.getDueDate().isBefore(java.time.LocalDateTime.now()));
    }

    public void addBorrowedBook(BorrowedBook borrowedBook) {
        borrowedBooks.add(borrowedBook);
        borrowedBook.setMember(this);
    }

    public void removeBorrowedBook(BorrowedBook borrowedBook) {
        borrowedBooks.remove(borrowedBook);
        borrowedBook.setMember(null);
    }
}
package com.gazmanzara.library.controller;

import com.gazmanzara.library.model.Book;
import com.gazmanzara.library.model.BorrowedBook;
import com.gazmanzara.library.model.Member;
import com.gazmanzara.library.model.BorrowStatus;
import com.gazmanzara.library.repository.BookRepository;
import com.gazmanzara.library.repository.BorrowedBookRepository;
import com.gazmanzara.library.repository.MemberRepository;
import com.gazmanzara.library.exception.ResourceNotFoundException;
import com.gazmanzara.library.exception.BadRequestException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/borrowed-books")
public class BorrowedBookController {

    private final BorrowedBookRepository borrowedBookRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public BorrowedBookController(
            BorrowedBookRepository borrowedBookRepository,
            BookRepository bookRepository,
            MemberRepository memberRepository) {
        this.borrowedBookRepository = borrowedBookRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @PostMapping("/borrow")
    public ResponseEntity<BorrowedBook> borrowBook(
            @RequestParam Long bookId,
            @RequestParam Long memberId,
            @RequestParam(required = false) Integer durationInDays) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (book.isCurrentlyBorrowed()) {
            throw new BadRequestException("Book is already borrowed");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));

        if (member.hasOverdueBooks()) {
            throw new BadRequestException("Member has overdue books and cannot borrow more books");
        }

        BorrowedBook borrowedBook = new BorrowedBook();
        borrowedBook.setBorrowDate(LocalDateTime.now());
        borrowedBook.setDueDate(LocalDateTime.now().plusDays(durationInDays != null ? durationInDays : 14));
        borrowedBook.setStatus(BorrowStatus.BORROWED);

        // Use helper methods to maintain bidirectional relationship
        book.addBorrowedBook(borrowedBook);
        member.addBorrowedBook(borrowedBook);

        BorrowedBook savedBorrow = borrowedBookRepository.save(borrowedBook);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedBorrow.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedBorrow);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<BorrowedBook> returnBook(@PathVariable Long id) {
        BorrowedBook borrowedBook = borrowedBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowedBook", "id", id));

        if (borrowedBook.getStatus() == BorrowStatus.RETURNED) {
            throw new BadRequestException("Book is already returned");
        }

        borrowedBook.setReturnDate(LocalDateTime.now());
        borrowedBook.setStatus(BorrowStatus.RETURNED);

        return ResponseEntity.ok(borrowedBookRepository.save(borrowedBook));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<BorrowedBook>> getMemberBorrows(
            @PathVariable Long memberId,
            @RequestParam(required = false) Boolean current) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));

        if (Boolean.TRUE.equals(current)) {
            return ResponseEntity.ok(List.copyOf(member.getCurrentBorrows()));
        }

        // Return all borrow records for the member (both current and historical)
        return ResponseEntity.ok(List.copyOf(member.getBorrowedBooks()));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<BorrowedBook>> getBookBorrowHistory(@PathVariable Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));
        return ResponseEntity.ok(borrowedBookRepository.findByBookIdOrderByBorrowDateDesc(bookId));
    }

    @GetMapping("/due-before")
    public ResponseEntity<List<BorrowedBook>> getBorrowsDueBefore(
            @RequestParam(required = false) LocalDateTime date) {

        LocalDateTime checkDate = date != null ? date : LocalDateTime.now();
        return ResponseEntity.ok(
                borrowedBookRepository.findByStatusAndDueDateBefore(BorrowStatus.BORROWED, checkDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowedBook> getBorrowById(@PathVariable Long id) {
        BorrowedBook borrowedBook = borrowedBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowedBook", "id", id));
        return ResponseEntity.ok(borrowedBook);
    }

    @GetMapping
    public ResponseEntity<List<BorrowedBook>> getAllBorrowedBooks(
            @RequestParam(required = false) BorrowStatus status) {
        if (status != null) {
            return ResponseEntity.ok(borrowedBookRepository.findByStatus(status));
        }
        return ResponseEntity.ok(borrowedBookRepository.findAll());
    }
}
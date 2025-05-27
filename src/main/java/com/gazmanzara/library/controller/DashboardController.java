package com.gazmanzara.library.controller;

import com.gazmanzara.library.model.*;
import com.gazmanzara.library.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowedBookRepository borrowedBookRepository;
    private final CategoryRepository categoryRepository;

    public DashboardController(
            BookRepository bookRepository,
            MemberRepository memberRepository,
            BorrowedBookRepository borrowedBookRepository,
            CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.borrowedBookRepository = borrowedBookRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> overview = new HashMap<>();

        // Book Statistics
        long totalBooks = bookRepository.count();
        long borrowedBooks = bookRepository.findAll().stream()
                .filter(Book::isCurrentlyBorrowed)
                .count();

        // Member Statistics
        long totalMembers = memberRepository.count();
        long activeMembers = memberRepository.findAll().stream()
                .filter(member -> !member.getCurrentBorrows().isEmpty())
                .count();
        long membersWithOverdue = memberRepository.findAll().stream()
                .filter(Member::hasOverdueBooks)
                .count();

        // Current Statistics
        overview.put("totalBooks", totalBooks);
        overview.put("borrowedBooks", borrowedBooks);
        overview.put("availableBooks", totalBooks - borrowedBooks);
        overview.put("totalMembers", totalMembers);
        overview.put("activeMembers", activeMembers);
        overview.put("membersWithOverdue", membersWithOverdue);

        return ResponseEntity.ok(overview);
    }

    @GetMapping("/books/popular")
    public ResponseEntity<List<Map<String, Object>>> getPopularBooks() {
        List<Book> allBooks = bookRepository.findAll();

        return ResponseEntity.ok(allBooks.stream()
                .map(book -> {
                    Map<String, Object> bookStats = new HashMap<>();
                    bookStats.put("id", book.getId());
                    bookStats.put("title", book.getTitle());
                    bookStats.put("author", book.getAuthor().getName());
                    bookStats.put("borrowCount", book.getBorrowedBooks().size());
                    return bookStats;
                })
                .sorted((b1, b2) -> ((Integer) b2.get("borrowCount")).compareTo((Integer) b1.get("borrowCount")))
                .limit(10)
                .collect(Collectors.toList()));
    }

    @GetMapping("/books/by-category")
    public ResponseEntity<List<Map<String, Object>>> getBooksByCategory() {
        List<Category> categories = categoryRepository.findAll();

        return ResponseEntity.ok(categories.stream()
                .map(category -> {
                    Map<String, Object> categoryStats = new HashMap<>();
                    categoryStats.put("category", category.getName());
                    categoryStats.put("bookCount", category.getBooks().size());
                    return categoryStats;
                })
                .collect(Collectors.toList()));
    }

    @GetMapping("/members/top-borrowers")
    public ResponseEntity<List<Map<String, Object>>> getTopBorrowers() {
        List<Member> members = memberRepository.findAll();

        return ResponseEntity.ok(members.stream()
                .map(member -> {
                    Map<String, Object> memberStats = new HashMap<>();
                    memberStats.put("id", member.getId());
                    memberStats.put("name", member.getFirstName() + " " + member.getLastName());
                    memberStats.put("borrowCount", member.getBorrowedBooks().size());
                    memberStats.put("currentBorrows", member.getCurrentBorrows().size());
                    return memberStats;
                })
                .sorted((m1, m2) -> ((Integer) m2.get("borrowCount")).compareTo((Integer) m1.get("borrowCount")))
                .limit(10)
                .collect(Collectors.toList()));
    }

    @GetMapping("/borrows/overdue")
    public ResponseEntity<List<Map<String, Object>>> getOverdueBooks() {
        List<BorrowedBook> overdueBooks = borrowedBookRepository
                .findByStatusAndDueDateBefore(BorrowStatus.BORROWED, LocalDateTime.now());

        return ResponseEntity.ok(overdueBooks.stream()
                .map(borrow -> {
                    Map<String, Object> borrowStats = new HashMap<>();
                    borrowStats.put("bookTitle", borrow.getBook().getTitle());
                    borrowStats.put("memberName",
                            borrow.getMember().getFirstName() + " " + borrow.getMember().getLastName());
                    borrowStats.put("dueDate", borrow.getDueDate());
                    borrowStats.put("daysOverdue",
                            LocalDateTime.now().getDayOfYear() - borrow.getDueDate().getDayOfYear());
                    return borrowStats;
                })
                .collect(Collectors.toList()));
    }

    @GetMapping("/borrows/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentBorrows() {
        List<BorrowedBook> recentBorrows = borrowedBookRepository.findAll().stream()
                .sorted((b1, b2) -> b2.getBorrowDate().compareTo(b1.getBorrowDate()))
                .limit(10)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recentBorrows.stream()
                .map(borrow -> {
                    Map<String, Object> borrowStats = new HashMap<>();
                    borrowStats.put("bookTitle", borrow.getBook().getTitle());
                    borrowStats.put("memberName",
                            borrow.getMember().getFirstName() + " " + borrow.getMember().getLastName());
                    borrowStats.put("borrowDate", borrow.getBorrowDate());
                    borrowStats.put("dueDate", borrow.getDueDate());
                    borrowStats.put("status", borrow.getStatus());
                    return borrowStats;
                })
                .collect(Collectors.toList()));
    }
}
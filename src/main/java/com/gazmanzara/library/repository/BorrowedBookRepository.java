package com.gazmanzara.library.repository;

import com.gazmanzara.library.model.BorrowedBook;
import com.gazmanzara.library.model.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BorrowedBookRepository extends JpaRepository<BorrowedBook, Long> {

    // Find all borrowed books for a specific member
    List<BorrowedBook> findByMemberIdAndStatus(Long memberId, BorrowStatus status);

    // Find current borrowed book for a specific book (if any)
    Optional<BorrowedBook> findByBookIdAndStatus(Long bookId, BorrowStatus status);

    // Find all borrowed books that are due before a certain date
    List<BorrowedBook> findByStatusAndDueDateBefore(BorrowStatus status, LocalDateTime date);

    // Check if a book is currently borrowed
    boolean existsByBookIdAndStatus(Long bookId, BorrowStatus status);

    // Find all borrowed books for a member that are currently borrowed
    @Query("SELECT bb FROM BorrowedBook bb WHERE bb.member.id = :memberId AND bb.status = 'BORROWED'")
    List<BorrowedBook> findCurrentBorrowsForMember(@Param("memberId") Long memberId);

    // Find all borrowed books for a book (borrowing history)
    List<BorrowedBook> findByBookIdOrderByBorrowDateDesc(Long bookId);

    // Find all borrowed books by status
    List<BorrowedBook> findByStatus(BorrowStatus status);
}
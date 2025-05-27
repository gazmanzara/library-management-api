package com.gazmanzara.library.repository;

import com.gazmanzara.library.model.Book;
import com.gazmanzara.library.model.Author;
import com.gazmanzara.library.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthor(Author author);

    List<Book> findByAuthorId(Long authorId);

    List<Book> findByCategoriesContaining(Category category);

    List<Book> findByPublicationYear(Integer year);

    @Query("SELECT DISTINCT b FROM Book b WHERE NOT EXISTS (SELECT bb FROM BorrowedBook bb WHERE bb.book = b AND bb.status = 'BORROWED')")
    List<Book> findActiveBooks();

    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowedBooks bb WHERE bb.status = 'BORROWED'")
    List<Book> findCurrentlyBorrowedBooks();
}

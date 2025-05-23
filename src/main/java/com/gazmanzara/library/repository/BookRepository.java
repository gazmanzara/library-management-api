package com.gazmanzara.library.repository;

import com.gazmanzara.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}

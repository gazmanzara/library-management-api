package com.gazmanzara.library.repository;

import com.gazmanzara.library.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    Optional<Category> findByName(String name);
    List<Category> findByNameContainingIgnoreCase(String name);
}

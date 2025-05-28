package com.gazmanzara.library.dto;

import com.gazmanzara.library.model.Book;

import java.util.Set;
import java.util.stream.Collectors;

public class BookDTO {
    public Long id;
    public String title;
    public String description;
    public String imgUrl;
    public String isbn;
    public Integer publicationYear;
    public AuthorDTO author;
    public Set<CategoryDTO> categories;

    public BookDTO(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.description = book.getDescription();
        this.imgUrl = book.getImgUrl();
        this.isbn = book.getIsbn();
        this.publicationYear = book.getPublicationYear();
        this.author = new AuthorDTO(book.getAuthor());
        this.categories = book.getCategories().stream().map(CategoryDTO::new).collect(Collectors.toSet());
    }
}

package com.gazmanzara.library.dto;

import com.gazmanzara.library.model.Category;

public class CategoryDTO {
    public Long id;
    public String name;
    public String description;

    public CategoryDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
    }
}

package com.vbartere.Advertisement.Model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "category")

public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotEmpty(message = "Category name can not be empty") @Size(min = 2, max = 20, message = "The minimum allowed number of characters is 2, the maximum is 20") String getName() {
        return name;
    }

    public void setName(@NotEmpty(message = "Category name can not be empty") @Size(min = 2, max = 20, message = "The minimum allowed number of characters is 2, the maximum is 20") String name) {
        this.name = name;
    }

    public List<SubCategory> getSubCategoryList() {
        return subCategoryList;
    }

    public void setSubCategoryList(List<SubCategory> subCategoryList) {
        this.subCategoryList = subCategoryList;
    }

    @Column(name = "name")
    @NotEmpty(message = "Category name can not be empty")
    @Size(min = 2, max = 20, message = "The minimum allowed number of characters is 2, the maximum is 20")
    private String name;

    @OneToMany(mappedBy = "parentCategory", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JsonManagedReference
    private List<SubCategory> subCategoryList;

    public Category() {}

    public Category(String name, List<SubCategory> subCategoryList) {
        this.name = name;
        this.subCategoryList = subCategoryList;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", subCategoryList=" + subCategoryList +
                '}';
    }
}

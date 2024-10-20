package com.vbartere.Advertisement.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "category")
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    @NotEmpty(message = "Category name can not be empty")
    @Size(min = 2, max = 20, message = "The minimum allowed number of characters is 2, the maximum is 20")
    private String name;

    @OneToMany(mappedBy = "parentCategory", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<SubCategory> subCategoryList;

    public Category() {}

    public Category(String name, List<SubCategory> subCategoryList) {
        this.name = name;
        this.subCategoryList = subCategoryList;
    }

//    @Override
//    public String toString() {
//        return "Category{" +
//                "id=" + id +
//                ", name='" + name + '\'' +
//                ", subCategoryList=" + subCategoryList +
//                '}';
//    }
}

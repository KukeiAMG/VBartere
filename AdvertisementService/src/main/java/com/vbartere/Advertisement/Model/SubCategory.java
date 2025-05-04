package com.vbartere.Advertisement.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "subcategory")
public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    @NotEmpty(message = "Subcategory name can not be empty")
    @Size(min = 2, max = 20, message = "The minimum allowed number of characters is 2, the maximum is 20")
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private Category parentCategory;

    @OneToMany(mappedBy = "subcategory", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JsonManagedReference
    private List<Advertisement> advertisementList;

    public SubCategory() {}

    public SubCategory(String name, Category parentCategory, List<Advertisement> advertisementList) {
        this.name = name;
        this.parentCategory = parentCategory;
        this.advertisementList = advertisementList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotEmpty(message = "Subcategory name can not be empty") @Size(min = 2, max = 20, message = "The minimum allowed number of characters is 2, the maximum is 20") String getName() {
        return name;
    }

    public void setName(@NotEmpty(message = "Subcategory name can not be empty") @Size(min = 2, max = 20, message = "The minimum allowed number of characters is 2, the maximum is 20") String name) {
        this.name = name;
    }

    public Category getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }

    public List<Advertisement> getAdvertisementList() {
        return advertisementList;
    }

    public void setAdvertisementList(List<Advertisement> advertisementList) {
        this.advertisementList = advertisementList;
    }

    //    @Override
//    public String toString() {
//        return "SubCategory{" +
//                "id=" + id +
//                ", name='" + name + '\'' +
//                ", parentCategory=" + parentCategory +
//                ", advertisementList=" + advertisementList +
//                '}';
//    }
}

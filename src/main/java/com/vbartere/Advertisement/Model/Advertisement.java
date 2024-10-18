package com.vbartere.Advertisement.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "advertisement")
@Getter
@Setter
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    @NotEmpty(message = "Title can not be empty")
    @Size(min = 2, max = 50, message = "The minimum allowed number of characters is 2, the maximum is 50")
    private String title;

    @Column(name = "description")
    @NotEmpty(message = "Description can not be empty")
    @Size(min = 2, max = 500, message = "The minimum allowed number of characters is 2, the maximum is 500")
    private String description;

    @ManyToOne
    @JoinColumn(name = "subcategory_id")
    private SubCategory subcategory;

    @OneToMany(mappedBy = "advertisement", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Image> imageList;

    private boolean status;

    public Advertisement() {}

    public Advertisement(String title, String description, SubCategory subcategory, List<Image> imageList, boolean status) {
        this.title = title;
        this.description = description;
        this.subcategory = subcategory;
        this.imageList = imageList;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Advertisement{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", subCategory=" + subcategory +
                ", imageList=" + imageList +
                ", status=" + status +
                '}';
    }
}

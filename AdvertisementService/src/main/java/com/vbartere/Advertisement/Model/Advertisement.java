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
@Table(name = "advertisement")
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
    @JsonBackReference
    private SubCategory subcategory;

    @OneToMany(mappedBy = "advertisement", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    @JsonManagedReference
    private List<Image> imageList;

    @Column(name = "ownerId")
    private Long ownerId; // id пользователя из UserService (owner)

    @Column(name = "buyersId")
    private Long buyersId;

    @Column(name = "status")
    private Boolean status;

    public Advertisement() {}

    public Advertisement(String title, String description, SubCategory subcategory, List<Image> imageList, Long ownerId, Long buyersId, boolean status) {
        this.title = title;
        this.description = description;
        this.subcategory = subcategory;
        this.imageList = imageList;
        this.ownerId = ownerId;
        this.buyersId = buyersId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotEmpty(message = "Title can not be empty") @Size(min = 2, max = 50, message = "The minimum allowed number of characters is 2, the maximum is 50") String getTitle() {
        return title;
    }

    public void setTitle(@NotEmpty(message = "Title can not be empty") @Size(min = 2, max = 50, message = "The minimum allowed number of characters is 2, the maximum is 50") String title) {
        this.title = title;
    }

    public @NotEmpty(message = "Description can not be empty") @Size(min = 2, max = 500, message = "The minimum allowed number of characters is 2, the maximum is 500") String getDescription() {
        return description;
    }

    public void setDescription(@NotEmpty(message = "Description can not be empty") @Size(min = 2, max = 500, message = "The minimum allowed number of characters is 2, the maximum is 500") String description) {
        this.description = description;
    }

    public SubCategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(SubCategory subcategory) {
        this.subcategory = subcategory;
    }

    public List<Image> getImageList() {
        return imageList;
    }

    public void setImageList(List<Image> imageList) {
        this.imageList = imageList;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getBuyersId() {
        return buyersId;
    }

    public void setBuyersId(Long buyersId) {
        this.buyersId = buyersId;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Advertisement{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", subcategory=" + subcategory +
                ", imageList=" + imageList +
                ", ownerId=" + ownerId +
                ", buyersId=" + buyersId +
                ", status=" + status +
                '}';
    }
}

package com.vbartere.AdminService.Model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admin_advertisement")
public class AdminAdvertisement {
    @Id
    private Long id;

    private String title;

    private String description;

    private Long subcategoryId;

    private String subcategoryTitle;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "admin_advertisement_image_urls", joinColumns = @JoinColumn(name = "advertisement_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    private Long ownerId;

    private String ownerUsername;

    private Long buyersId;
    
    private String buyerUsername;

    private Boolean status;

    private Boolean banStatus;

    public AdminAdvertisement() {}

    public AdminAdvertisement(Long id, String title, String description, Long subcategoryId, String subcategoryTitle, List<String> imageUrls, Long ownerId, String ownerUsername, Long buyersId, String buyerUsername, Boolean status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.subcategoryId = subcategoryId;
        this.subcategoryTitle = subcategoryTitle;
        this.imageUrls = imageUrls;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.buyersId = buyersId;
        this.buyerUsername = buyerUsername;
        this.status = status;
        this.banStatus = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(Long subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public String getSubcategoryTitle() {
        return subcategoryTitle;
    }

    public void setSubcategoryTitle(String subcategoryTitle) {
        this.subcategoryTitle = subcategoryTitle;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public Long getBuyersId() {
        return buyersId;
    }

    public void setBuyersId(Long buyersId) {
        this.buyersId = buyersId;
    }

    public String getBuyerUsername() {
        return buyerUsername;
    }

    public void setBuyerUsername(String buyerUsername) {
        this.buyerUsername = buyerUsername;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getBanStatus() {
        return banStatus;
    }

    public void setBanStatus(Boolean banStatus) {
        this.banStatus = banStatus;
    }
}

package com.vbartere.userservice.model;

import com.vbartere.userservice.DTO.AdvertisementDTO;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @ElementCollection
    @CollectionTable(name = "cart_advertisement_list", joinColumns = @JoinColumn(name = "cart_id"))
    @Column(name = "advertisement_id")
    List<Long> advertisementList;
    @Column(name = "user_id")
    private Long userId;

    public Cart() {

    }

    public Cart(List<Long> advertisementList, Long userId) {
        this.advertisementList = advertisementList;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Long> getAdvertisementList() {
        return advertisementList;
    }

    public void setAdvertisementList(List<Long> advertisementList) {
        this.advertisementList = advertisementList;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", advertisementList=" + advertisementList +
                ", userId=" + userId +
                '}';
    }
}

package com.vbartere.userservice.model;

import com.vbartere.userservice.model.Embeddable.CartItem;
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
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    @Column(name = "advertisement_id")
    List<CartItem> advertisementList;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public Cart() {

    }

    public Cart(List<CartItem> advertisementList, User user) {
        this.advertisementList = advertisementList;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CartItem> getAdvertisementList() {
        return advertisementList;
    }

    public void setAdvertisementList(List<CartItem> advertisementList) {
        this.advertisementList = advertisementList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", advertisementList=" + advertisementList +
                ", user=" + user +
                '}';
    }
}

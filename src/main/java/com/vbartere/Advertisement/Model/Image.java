package com.vbartere.Advertisement.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "image")
@Getter
@Setter
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "size")
    private Long size;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @ManyToOne
    @JoinColumn(name = "advertisement_id")
    private Advertisement advertisement;

    public Image() {}

    public Image(Long size, String name, String url, Advertisement advertisement) {
        this.size = size;
        this.name = name;
        this.url = url;
        this.advertisement = advertisement;
    }

    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", size=" + size +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", advertisement=" + advertisement +
                '}';
    }
}

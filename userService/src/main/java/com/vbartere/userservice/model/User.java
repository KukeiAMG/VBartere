package com.vbartere.userservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Getter
@Setter
@Entity
@Table(name="users")
public class User {
    @Id
    private Long id;

    private String name;
    private String surname;
    private String status;
    private String email;
    private String password;


    public User(String email, String password, String name, String surname, Long id) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.id = id;
    }
    public User() {}


}

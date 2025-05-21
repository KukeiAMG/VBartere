package com.vbartere.userservice.repository;

import com.vbartere.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumberWithRoles(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cart c LEFT JOIN FETCH c.advertisementList WHERE u.id = :id")
    Optional<User> findByIdWithCartAndAds(@Param("id") Long id);
}

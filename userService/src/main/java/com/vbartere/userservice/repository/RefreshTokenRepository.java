package com.vbartere.userservice.repository;

import com.vbartere.userservice.model.RefreshToken;
import com.vbartere.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteAllByUser(User user);

    List<RefreshToken> findAllByUser(User user);
}

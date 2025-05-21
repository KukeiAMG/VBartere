package com.vbartere.Advertisement.Repository;

import com.vbartere.Advertisement.Model.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    @Query("SELECT a FROM Advertisement a LEFT JOIN FETCH a.imageList WHERE a.id = :id")
    Optional<Advertisement> findByIdWithImages(@Param("id") Long id);
}

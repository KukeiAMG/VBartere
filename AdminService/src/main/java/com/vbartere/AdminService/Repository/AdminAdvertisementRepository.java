package com.vbartere.AdminService.Repository;

import com.vbartere.AdminService.Model.AdminAdvertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminAdvertisementRepository extends JpaRepository<AdminAdvertisement, Long> {
}

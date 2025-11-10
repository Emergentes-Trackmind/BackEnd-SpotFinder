package com.spotfinder.backend.v1.profile.infrastructure.persistence.jpa.repositories;



import com.spotfinder.backend.v1.profile.domain.model.aggregates.ParkingOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductOwnerRepository extends JpaRepository<ParkingOwner, Long> {
    Optional<ParkingOwner> findParkingOwnerByUserId(Long id);
    boolean existsByPhone_Phone(String phone);
    boolean existsByRuc_Ruc(String ruc);
    boolean existsByUserId(Long userId);

}

package com.spotfinder.backend.v1.profile.infrastructure.persistence.jpa.repositories;

import com.spotfinder.backend.v1.profile.domain.model.aggregates.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findDriverByUserId(Long userId);
    // Verificar existencia de DNI
    boolean existsByDni_Dni(String dni);

    // Verificar existencia de Phone
    boolean existsByPhone_Phone(String phone);

    boolean existsByUserId(Long userId);
}

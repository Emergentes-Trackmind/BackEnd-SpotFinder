package com.spotfinder.backend.v1.reservations.infrastructure.persistence.jpa.repositories;

import com.spotfinder.backend.v1.reservations.domain.model.aggregates.Reservation;
import com.spotfinder.backend.v1.reservations.domain.model.valueobjects.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findReservationsByParkingIdParkingId(Long parkingId);
    List<Reservation> findReservationsByDriverIdDriverId(Long driverId);
    List<Reservation> findReservationsByDriverIdDriverIdAndStatus(Long driverId, ReservationStatus status);
}

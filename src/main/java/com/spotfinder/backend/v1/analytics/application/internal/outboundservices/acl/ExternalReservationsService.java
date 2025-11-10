package com.spotfinder.backend.v1.analytics.application.internal.outboundservices.acl;

import com.spotfinder.backend.v1.reservations.domain.model.aggregates.Reservation;
import com.spotfinder.backend.v1.reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Analytics ACL to query reservations without depending directly on another bounded context's internals.
 */
@Component
public class ExternalReservationsService {
    private final ReservationRepository reservationRepository;

    public ExternalReservationsService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }
}


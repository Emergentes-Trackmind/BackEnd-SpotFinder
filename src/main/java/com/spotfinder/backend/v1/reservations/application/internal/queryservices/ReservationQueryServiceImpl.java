package com.spotfinder.backend.v1.reservations.application.internal.queryservices;

import com.spotfinder.backend.v1.reservations.domain.model.aggregates.Reservation;
import com.spotfinder.backend.v1.reservations.domain.model.queries.GetAllReservationsByDriverIdAndStatusQuery;
import com.spotfinder.backend.v1.reservations.domain.model.queries.GetAllReservationsByDriverIdQuery;
import com.spotfinder.backend.v1.reservations.domain.model.queries.GetAllReservationsByParkingIdQuery;
import com.spotfinder.backend.v1.reservations.domain.model.valueobjects.ReservationStatus;
import com.spotfinder.backend.v1.reservations.domain.services.ReservationQueryService;
import com.spotfinder.backend.v1.reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationQueryServiceImpl implements ReservationQueryService {
    private final ReservationRepository reservationRepository;

    public ReservationQueryServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<Reservation> handle(GetAllReservationsByParkingIdQuery query) {
        return reservationRepository.findReservationsByParkingIdParkingId(query.parkingId());
    }

    @Override
    public List<Reservation> handle(GetAllReservationsByDriverIdQuery query) {
        return reservationRepository.findReservationsByDriverIdDriverId(query.driverId());
    }

    @Override
    public List<Reservation> handle(GetAllReservationsByDriverIdAndStatusQuery query) {
        var reservationStatus = ReservationStatus.valueOf(query.status());
        return reservationRepository.findReservationsByDriverIdDriverIdAndStatus(query.driverId(), reservationStatus);
    }
}

package com.spotfinder.backend.v1.reservations.domain.services;

import com.spotfinder.backend.v1.reservations.domain.model.aggregates.Reservation;
import com.spotfinder.backend.v1.reservations.domain.model.queries.GetAllReservationsByDriverIdAndStatusQuery;
import com.spotfinder.backend.v1.reservations.domain.model.queries.GetAllReservationsByDriverIdQuery;
import com.spotfinder.backend.v1.reservations.domain.model.queries.GetAllReservationsByParkingIdQuery;
import com.spotfinder.backend.v1.reservations.domain.model.queries.GetAllReservationsQuery;

import java.util.List;

public interface ReservationQueryService {
    List<Reservation> handle(GetAllReservationsByParkingIdQuery query);
    List<Reservation> handle(GetAllReservationsByDriverIdQuery query);
    List<Reservation> handle(GetAllReservationsByDriverIdAndStatusQuery query);
    List<Reservation> handle(GetAllReservationsQuery query);
}

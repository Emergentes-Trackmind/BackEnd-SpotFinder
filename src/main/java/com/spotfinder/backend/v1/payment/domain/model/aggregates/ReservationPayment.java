package com.spotfinder.backend.v1.payment.domain.model.aggregates;

import com.spotfinder.backend.v1.payment.domain.model.commands.CreatePaymentCommand;
import com.spotfinder.backend.v1.payment.domain.model.valueobjects.Payment;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@DiscriminatorValue("RESERVATION")
@NoArgsConstructor
public class ReservationPayment extends Payment {

    @Column(name = "reservation_id")
    private Long reservationId;

    public ReservationPayment(CreatePaymentCommand command, Long reservationId) {
        super(command);
        this.reservationId = reservationId;
    }

    // Explicit getter to ensure availability at compile time
    public Long getReservationId() {
        return reservationId;
    }

    @Override
    public boolean isForSubscription() {
        return false;
    }

    @Override
    public boolean isForReservation() {
        return true;
    }
}
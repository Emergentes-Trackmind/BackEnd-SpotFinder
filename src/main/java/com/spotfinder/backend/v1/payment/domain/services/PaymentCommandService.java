package com.spotfinder.backend.v1.payment.domain.services;

import com.spotfinder.backend.v1.payment.domain.model.aggregates.ReservationPayment;
import com.spotfinder.backend.v1.payment.domain.model.aggregates.SubscriptionPayment;
import com.spotfinder.backend.v1.payment.domain.model.commands.CreatePaymentCommand;

import java.util.Optional;

public interface PaymentCommandService {

    Optional<ReservationPayment> handleReservationPayment(CreatePaymentCommand command,
                                                           Long reservationId);

    Optional<SubscriptionPayment> handleSubscriptionPayment(CreatePaymentCommand command,
                                                            Long subscriptionId);
}
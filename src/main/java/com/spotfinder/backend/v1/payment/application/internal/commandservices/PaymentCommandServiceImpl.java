package com.spotfinder.backend.v1.payment.application.internal.commandservices;

import com.spotfinder.backend.v1.payment.domain.model.aggregates.ReservationPayment;
import com.spotfinder.backend.v1.payment.domain.model.aggregates.SubscriptionPayment;
import com.spotfinder.backend.v1.payment.domain.model.commands.CreatePaymentCommand;
import com.spotfinder.backend.v1.payment.domain.services.PaymentCommandService;
import com.spotfinder.backend.v1.payment.infrastructure.persistence.jpa.repositories.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentCommandServiceImpl implements PaymentCommandService {
    private final PaymentRepository paymentRepository;

    public PaymentCommandServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }


    @Override
    public Optional<ReservationPayment> handleReservationPayment(CreatePaymentCommand command, Long reservationId) {
        var reservationPayment = new ReservationPayment(command, reservationId);
        var createdPayment = paymentRepository.save(reservationPayment);
        return Optional.of(createdPayment);
    }

    @Override
    public Optional<SubscriptionPayment> handleSubscriptionPayment(CreatePaymentCommand command, Long subscriptionId) {
        var subscriptionPayment = new SubscriptionPayment(command, subscriptionId);
        var createdPayment = paymentRepository.save(subscriptionPayment);
        return Optional.of(createdPayment);
    }
}

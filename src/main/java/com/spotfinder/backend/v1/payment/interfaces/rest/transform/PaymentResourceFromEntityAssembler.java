package com.spotfinder.backend.v1.payment.interfaces.rest.transform;

import com.spotfinder.backend.v1.payment.domain.model.aggregates.ReservationPayment;
import com.spotfinder.backend.v1.payment.domain.model.aggregates.SubscriptionPayment;
import com.spotfinder.backend.v1.payment.domain.model.valueobjects.Payment;
import com.spotfinder.backend.v1.payment.interfaces.rest.resources.PaymentResource;

public class PaymentResourceFromEntityAssembler {
    public static PaymentResource toResourceFromEntity(Payment payment) {
        return switch (payment) {
            case ReservationPayment reservationPayment -> new PaymentResource(
                    "ReservationPayment",
                    reservationPayment.getId(),
                    reservationPayment.getAmount(),
                    reservationPayment.getPaidAt().toString(),
                    reservationPayment.getReservationId(),
                    null
            );
            case SubscriptionPayment subscriptionPayment -> new PaymentResource(
                    "SubscriptionPayment",
                    subscriptionPayment.getId(),
                    subscriptionPayment.getAmount(),
                    subscriptionPayment.getPaidAt().toString(),
                    null,
                    subscriptionPayment.getSubscriptionId()
            );
            default -> null;
        };
    }
}

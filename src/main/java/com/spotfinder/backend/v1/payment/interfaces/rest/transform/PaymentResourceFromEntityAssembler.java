package com.spotfinder.backend.v1.payment.interfaces.rest.transform;

import com.spotfinder.backend.v1.payment.domain.model.aggregates.ReservationPayment;
import com.spotfinder.backend.v1.payment.domain.model.aggregates.SubscriptionPayment;
import com.spotfinder.backend.v1.payment.domain.model.valueobjects.Payment;
import com.spotfinder.backend.v1.payment.interfaces.rest.resources.PaymentResource;

public class PaymentResourceFromEntityAssembler {
    public static PaymentResource toResourceFromEntity(Payment payment) {
        if (payment instanceof ReservationPayment reservationPayment) {
            return new PaymentResource(
                    "ReservationPayment",
                    reservationPayment.getId(),
                    reservationPayment.getAmount(),
                    reservationPayment.getPaidAt().toString(),
                    reservationPayment.getReservationId(),
                    null
            );
        } else if (payment instanceof SubscriptionPayment subscriptionPayment) {
            return new PaymentResource(
                    "SubscriptionPayment",
                    subscriptionPayment.getId(),
                    subscriptionPayment.getAmount(),
                    subscriptionPayment.getPaidAt().toString(),
                    null,
                    subscriptionPayment.getSubscriptionId()
            );
        }
        return null;
    }
}

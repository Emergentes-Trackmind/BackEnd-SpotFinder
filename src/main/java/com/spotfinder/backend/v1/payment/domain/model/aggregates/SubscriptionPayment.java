package com.spotfinder.backend.v1.payment.domain.model.aggregates;

import com.spotfinder.backend.v1.payment.domain.model.commands.CreatePaymentCommand;
import com.spotfinder.backend.v1.payment.domain.model.valueobjects.Payment;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("SUBSCRIPTION")
@NoArgsConstructor
@Getter
public class SubscriptionPayment extends Payment {

    @Column(name = "subscription_id")
    private Long subscriptionId;

    public SubscriptionPayment(CreatePaymentCommand command, Long subscriptionId) {
        super(command);
        this.subscriptionId = subscriptionId;
    }

    @Override
    public boolean isForSubscription() {
        return true;
    }

    @Override
    public boolean isForReservation() {
        return false;
    }
}
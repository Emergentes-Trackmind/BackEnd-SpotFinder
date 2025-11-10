package com.spotfinder.backend.v1.payment.domain.model.valueobjects;

import com.spotfinder.backend.v1.payment.domain.model.commands.CreatePaymentCommand;
import com.spotfinder.backend.v1.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type")
@NoArgsConstructor
@Getter
public abstract class Payment extends AuditableAbstractAggregateRoot<Payment> {

    private Long userId;

    @Column(nullable = false)
    private double amount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @NotBlank
    private String nameOnCard;

    @NotBlank
    private String cardNumber;

    @NotBlank
    private String cardExpiryDate;

    protected Payment(CreatePaymentCommand command) {
        this.paidAt = LocalDateTime.now();
        this.userId = command.userId();
        this.amount = command.amount();
        this.nameOnCard = command.nameOnCard();
        this.cardNumber = command.cardNumber();
        this.cardExpiryDate = command.cardExpiryDate();
    }

    public abstract boolean isForSubscription();
    public abstract boolean isForReservation();
}
package com.spotfinder.backend.v1.payment.domain.model.commands;

public record CreatePaymentCommand(
        Long userId,
        double amount,
        String nameOnCard,
        String cardNumber,
        String cardExpiryDate
) {
    public CreatePaymentCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        if (nameOnCard == null || nameOnCard.isBlank()) {
            throw new IllegalArgumentException("Name on card cannot be blank.");
        }
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new IllegalArgumentException("Card number cannot be blank.");
        }
        if (cardExpiryDate == null || cardExpiryDate.isBlank()) {
            throw new IllegalArgumentException("Card expiry date cannot be blank.");
        }
    }
}

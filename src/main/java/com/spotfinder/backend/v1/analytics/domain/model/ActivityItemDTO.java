package com.spotfinder.backend.v1.analytics.domain.model;

public record ActivityItemDTO(
        String id,
        String type, // 'reservation_confirmed' | 'payment_processed' | 'reservation_cancelled' | 'parking_created' | 'review_added'
        String title,
        String description,
        String userName,
        String userAvatar,
        String status, // 'confirmed' | 'paid' | 'cancelled' | 'created' | 'pending'
        String createdAt,
        RelatedEntity relatedEntity
) {
    public record RelatedEntity(String id, String name, String type) {}
}


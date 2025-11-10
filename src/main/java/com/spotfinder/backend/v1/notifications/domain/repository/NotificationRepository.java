package com.spotfinder.backend.v1.notifications.domain.repository;

import com.spotfinder.backend.v1.notifications.domain.model.NotificationMessage;

public interface NotificationRepository {
    void send(NotificationMessage message);
}

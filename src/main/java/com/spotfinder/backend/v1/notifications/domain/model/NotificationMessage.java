package com.spotfinder.backend.v1.notifications.domain.model;

public class NotificationMessage {
    private final String title;
    private final String body;
    private final String token;

    public NotificationMessage(String token, String title, String body) {
        this.token = token;
        this.title = title;
        this.body = body;
    }

    public String getToken() {
        return token;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

}
package com.spotfinder.backend.v1.iam.domain.model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Entity
public class RefreshToken {
    @Id
    private String token;
    private Long userId;
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    public RefreshToken() {}

    public RefreshToken(String token, Long userId, Date expiresAt) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.before(new Date());
    }
}


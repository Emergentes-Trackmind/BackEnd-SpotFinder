package com.spotfinder.backend.v1.iam.domain.model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Entity
public class PasswordResetToken {
    @Id
    private String token;
    private Long userId;
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;
    private boolean used;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, Long userId, Date expiresAt, boolean used) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.used = used;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public boolean isExpired() { return expiresAt != null && expiresAt.before(new Date()); }
}


package com.spotfinder.backend.v1.reviews.domain.model.aggregates;

import com.spotfinder.backend.v1.reviews.domain.model.commands.CreateReviewCommand;
import com.spotfinder.backend.v1.reviews.domain.model.valueobjects.DriverId;
import com.spotfinder.backend.v1.reviews.domain.model.valueobjects.ParkingId;
import com.spotfinder.backend.v1.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.Getter;

@Entity
public class Review extends AuditableAbstractAggregateRoot<Review> {

    @Embedded
    private DriverId driverId;

    @Getter
    private String driverName;

    @Embedded
    private ParkingId parkingId;

    @Getter
    private String parkingName;

    @Getter
    private String comment;

    @Getter
    private Float rating;

    // Missing field declarations
    private Boolean responded = false;
    private String responseText;
    private java.util.Date responseAt;
    private java.util.Date readAt;
    private Boolean archived = false;
    private java.util.Date archivedAt;
    private String userEmail;
    private String userAvatar;

    protected Review() {}

    public Review(CreateReviewCommand command, String driverName, String parkingName) {
        this.driverId = new DriverId(command.driverId());
        this.driverName = driverName;
        this.parkingId = new ParkingId(command.parkingId());
        this.parkingName = parkingName;
        this.comment = command.comment();
        this.rating = command.rating();
        this.responded = false;
        this.archived = false;
    }

    // Explicit getter methods to ensure they're available
    public String getDriverName() {
        return driverName;
    }

    public String getParkingName() {
        return parkingName;
    }

    public String getComment() {
        return comment;
    }

    public Float getRating() {
        return rating;
    }

    public String getResponseText() {
        return responseText;
    }

    public java.util.Date getResponseAt() {
        return responseAt;
    }

    public java.util.Date getReadAt() {
        return readAt;
    }

    public java.util.Date getArchivedAt() {
        return archivedAt;
    }

    public Long getDriverId() {
        return driverId.driverId();
    }

    public Long getParkingId() {
        return parkingId.parkingId();
    }

    public Boolean getResponded() {
        return responded;
    }

    public Boolean getArchived() {
        return archived;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void respondWithText(String text) {
        this.responded = true;
        this.responseText = text;
        this.responseAt = new java.util.Date();
    }

    public void markRead() {
        this.readAt = new java.util.Date();
    }

    public void archiveNow() {
        this.archived = true;
        this.archivedAt = new java.util.Date();
    }
}

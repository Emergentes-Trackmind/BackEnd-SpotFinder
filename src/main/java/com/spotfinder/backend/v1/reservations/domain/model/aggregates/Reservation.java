package com.spotfinder.backend.v1.reservations.domain.model.aggregates;

import com.spotfinder.backend.v1.reservations.domain.model.commands.CreateReservationCommand;
import com.spotfinder.backend.v1.reservations.domain.model.valueobjects.DriverId;
import com.spotfinder.backend.v1.reservations.domain.model.valueobjects.ParkingId;
import com.spotfinder.backend.v1.reservations.domain.model.valueobjects.ParkingSpotId;
import com.spotfinder.backend.v1.reservations.domain.model.valueobjects.ReservationStatus;
import com.spotfinder.backend.v1.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Reservation extends AuditableAbstractAggregateRoot<Reservation> {

    @Embedded
    private DriverId driverId;

    @Getter
    private String driverName;

    @Getter
    @NotNull
    private String vehiclePlate;

    @Embedded
    private ParkingId parkingId;

    @Embedded
    private ParkingSpotId parkingSpotId;

    @Getter
    private String spotLabel;

    @Getter
    private LocalDate date;

    @Getter
    private LocalTime startTime;

    @Getter
    private LocalTime endTime;

    @Getter
    private Float totalPrice;
    
    private ReservationStatus status;

    protected Reservation() {}

    public Reservation(CreateReservationCommand command, String driverName, Float pricePerHour,
                       String spotLabel) {
        this.driverName = driverName;
        this.driverId = new DriverId(command.driverId());
        this.vehiclePlate = command.vehiclePlate();
        this.parkingId = new ParkingId(command.parkingId());
        this.parkingSpotId = new ParkingSpotId(command.parkingSpotId());
        this.spotLabel = spotLabel;
        this.date = command.date();
        this.startTime = LocalTime.parse(command.startTime());
        this.endTime = LocalTime.parse(command.endTime());
        this.totalPrice = calculateTotalPrice(pricePerHour);
        this.status = ReservationStatus.PENDING;
    }

    private Float calculateTotalPrice(Float pricePerHour) {
        long minutes = java.time.Duration.between(startTime, endTime).toMinutes();
        return (minutes / 60.0f) * pricePerHour;
    }

    public void updateStatus(String newStatus) {
        String normalized = newStatus != null ? newStatus.toUpperCase() : "";
        if ("CANCELLED".equals(normalized)) normalized = "CANCELED";
        this.status = ReservationStatus.valueOf(normalized);
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELED;
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }

    public Long getDriverId() {
        return driverId.driverId();
    }

    public Long getParkingId() {
        return parkingId.parkingId();
    }

    public String getParkingSpotId() {
        return parkingSpotId.parkingSpotId().toString();
    }

    public String getStatus() {
        if (status == ReservationStatus.CANCELED) return "CANCELLED";
        return status.name();
    }
}

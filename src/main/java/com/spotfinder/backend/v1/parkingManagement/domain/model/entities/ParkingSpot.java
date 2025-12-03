package com.spotfinder.backend.v1.parkingManagement.domain.model.entities;

import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.ParkingSpotStatus;
import com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.ParkingSpotIotStatus;
import com.spotfinder.backend.v1.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Entity
public class ParkingSpot extends AuditableModel {

    @Id
    private final UUID id;

    @ManyToOne
    @JoinColumn(name = "parking_id", nullable = false)
    @NotNull
    private Parking parking;

    @Setter
    private Integer rowIndex;

    @Setter
    private Integer columnIndex;

    @Setter
    private String label;

    @Setter
    @Enumerated(EnumType.STRING)
    private ParkingSpotStatus status;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "iot_status")
    private ParkingSpotIotStatus iotStatus;

    @Setter
    @Column(name = "sensor_serial_number", unique = true)
    private String sensorSerialNumber;

    protected ParkingSpot() {
        this.id = null;
    }

    public ParkingSpot(Parking parking, Integer row, Integer column, String label) {
        this.id = UUID.randomUUID();
        this.parking = parking;
        this.rowIndex = row;
        this.columnIndex = column;
        this.label = label;
        this.status = ParkingSpotStatus.AVAILABLE;
        this.iotStatus = ParkingSpotIotStatus.OFFLINE;
        this.sensorSerialNumber = null;
    }

    // Explicit getId() method to ensure it's available
    public UUID getId() {
        return id;
    }

    // Explicit getLabel() method to ensure it's available
    public String getLabel() {
        return label;
    }

    // Additional missing getter methods
    public Integer getRowIndex() {
        return rowIndex;
    }

    public Integer getColumnIndex() {
        return columnIndex;
    }

    public Long getParkingId() {
        return this.parking.getId();
    }

    public void updateStatus(String status) {
        this.status = ParkingSpotStatus.valueOf(status);
    }

    public String getStatus() {
        return this.status.name();
    }

    public ParkingSpotIotStatus getIotStatus() {
        return this.iotStatus;
    }

    public String getSensorSerialNumber() {
        return this.sensorSerialNumber;
    }

    public void assignIotSensor(String serialNumber) {
        this.sensorSerialNumber = serialNumber;
        this.iotStatus = ParkingSpotIotStatus.OFFLINE;
    }

    public void updateByTelemetry(boolean occupied) {
        this.status = occupied ? ParkingSpotStatus.OCCUPIED : ParkingSpotStatus.AVAILABLE;
        this.iotStatus = ParkingSpotIotStatus.CONNECTED;
    }
}

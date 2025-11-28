package com.spotfinder.backend.v1.deviceManagement.domain.model.aggregates;

import com.spotfinder.backend.v1.deviceManagement.domain.model.commands.UpdateDeviceCommand;
import com.spotfinder.backend.v1.deviceManagement.domain.model.valueobjects.*;
import com.spotfinder.backend.v1.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Device extends AuditableAbstractAggregateRoot<Device> {

    @Embedded
    private ParkingId parkingId;

    @Embedded
    private ParkingSpotId parkingSpotId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SpotStatus spotStatus;

    private String spotLabel;

    @Embedded
    private EdgeServerId edgeServerId;

    private String macAddress;

    @Enumerated(EnumType.STRING)
    private DeviceType type;

    @Enumerated(EnumType.STRING)
    private DeviceStatus operationalStatus;

    private LocalDateTime lastCommunication;

    protected Device() {}

    public Device(Long parkingId, UUID parkingSpotId, String spotStatus, String spotLabel, String edgeServerId) {
        this.parkingId = new ParkingId(parkingId);
        this.parkingSpotId = new ParkingSpotId(parkingSpotId);
        this.spotStatus = SpotStatus.valueOf(spotStatus);
        this.spotLabel = spotLabel;
        this.edgeServerId = new EdgeServerId(edgeServerId);
        this.macAddress = " ";
        this.type = DeviceType.NONE;
        this.operationalStatus = DeviceStatus.OFFLINE;
        this.lastCommunication = LocalDateTime.now();
    }

    public void updateMissingFields(UpdateDeviceCommand command) {
        this.edgeServerId = new EdgeServerId(command.edgeServerId());
        this.macAddress = command.macAddress();
        this.type = DeviceType.valueOf(command.type());
        this.operationalStatus = DeviceStatus.ONLINE;
        this.lastCommunication = LocalDateTime.now();
    }

    // Explicit getter methods
    public String getMacAddress() {
        return macAddress;
    }

    public String getSpotLabel() {
        return spotLabel;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setLastCommunication(LocalDateTime lastCommunication) {
        this.lastCommunication = lastCommunication;
    }

    public Long getParkingId() {
        return parkingId.parkingId();
    }

    public String getType() {
        return type.name();
    }

    public String getOperationalStatus() {
        return operationalStatus.name();
    }

    public String getSpotStatus() {
        return spotStatus.name();
    }

    public String getLastCommunication() {
        return lastCommunication.toString();
    }

    public String getParkingSpotId() {
        return parkingSpotId.spotId().toString();
    }

    public String getEdgeServerId() {
        return edgeServerId.edgeServerId() == null ? null : edgeServerId.edgeServerId();
    }
}

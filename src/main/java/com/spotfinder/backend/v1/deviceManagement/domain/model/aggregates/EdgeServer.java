package com.spotfinder.backend.v1.deviceManagement.domain.model.aggregates;

import com.spotfinder.backend.v1.deviceManagement.domain.model.commands.CreateEdgeServerCommand;
import com.spotfinder.backend.v1.deviceManagement.domain.model.valueobjects.EdgeServerStatus;
import com.spotfinder.backend.v1.deviceManagement.domain.model.valueobjects.ParkingId;
import com.spotfinder.backend.v1.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
public class EdgeServer extends AuditableAbstractAggregateRoot<EdgeServer> {

    @Getter
    @NotNull
    private String serverId;

    @Getter
    @NotNull
    private String apiKey;

    @Getter
    @NotNull
    private String name;

    @Setter
    @Getter
    @NotNull
    private String macAddress;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private EdgeServerStatus status;

    @Getter
    @Setter
    private LocalDateTime lastHeartbeat;

    @Getter
    @Setter
    private Integer connectedDevicesCount;

    @Embedded
    private ParkingId parkingId;

    protected EdgeServer() {}

    public EdgeServer(Long parking) {
        this.parkingId = new ParkingId(parking);
        this.serverId = generateServerId(parking);
        this.apiKey = generateApiKey();
        this.name = generateName();
        this.macAddress = "00:00:00:00:00:00";
        this.status = EdgeServerStatus.ONLINE;
        this.lastHeartbeat = LocalDateTime.now();
        this.connectedDevicesCount = 0;
    }


    public EdgeServer(CreateEdgeServerCommand command) {
        this.serverId = command.serverId();
        this.apiKey = command.apiKey();
        this.name = command.name();
        this.macAddress = command.macAddress();
        this.status = EdgeServerStatus.valueOf(command.status());
        this.lastHeartbeat = LocalDateTime.now();
        this.connectedDevicesCount = 0;
        this.parkingId = new ParkingId(command.parkingId());
    }

    public String getLastHeartBeat() {
        return this.lastHeartbeat.toString();
    }

    public Long getParkingId() {
        return this.parkingId.parkingId();
    }


    private String generateServerId(Long parkingId) {
        var timestamp = LocalDateTime.now();
        var random = Math.random() * 1000; // Random number between 0 and 999
        return String.format("SP-%d-%s-%.0f", parkingId, timestamp.toString().replace(":", "").replace("-", ""), random);
    }

    private String generateApiKey() {
        var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder apiKey = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            apiKey.append(chars.charAt((int) Math.floor(Math.random() * chars.length())));
        }
        return apiKey.toString();
    };

    private String generateName() {
        return "EdgeServer-" + this.parkingId.parkingId();
    }
}

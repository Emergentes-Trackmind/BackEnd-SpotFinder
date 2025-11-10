package com.spotfinder.backend.v1.deviceManagement.infrastructure.persistence.jpa.repositories;

import com.spotfinder.backend.v1.deviceManagement.domain.model.aggregates.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByParkingSpotId_SpotId(UUID parkingSpotId);
    List<Device> findByParkingId_ParkingId(Long parkingId);
    List<Device> findByParkingIdParkingIdAndEdgeServerIdEdgeServerId(Long parkingId, String edgeServerId);
    Optional<Device> findByParkingSpotIdSpotId(UUID parkingSpotIdSpotId);
    List<Device> findByEdgeServerIdEdgeServerId(String edgeServerId);
    List<Device> findAllByEdgeServerId_EdgeServerId(String edgeServerId);
}

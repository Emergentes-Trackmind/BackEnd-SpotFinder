package com.spotfinder.backend.v1.deviceManagement.infrastructure.persistence.jpa.repositories;

import com.spotfinder.backend.v1.deviceManagement.domain.model.aggregates.EdgeServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EdgeServerRepository extends JpaRepository<EdgeServer, Long> {
    boolean existsByServerId(String serverId);
    Optional<EdgeServer> findByMacAddress(String macAddress);
    List<EdgeServer> findByParkingIdParkingId(Long parkingId);
    Optional<EdgeServer> findByParkingId_ParkingId(Long parkingId);

    Optional<EdgeServer> findByServerId(String serverId);
    @Query("SELECT e.parkingId.parkingId FROM EdgeServer e WHERE e.serverId = :serverId")
    Long findParkingIdByServerId(String serverId);
}

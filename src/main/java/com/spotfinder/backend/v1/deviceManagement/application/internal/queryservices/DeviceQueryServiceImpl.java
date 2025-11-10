package com.spotfinder.backend.v1.deviceManagement.application.internal.queryservices;

import com.spotfinder.backend.v1.deviceManagement.domain.model.aggregates.Device;
import com.spotfinder.backend.v1.deviceManagement.domain.model.queries.GetDeviceByParkingSpotIdQuery;
import com.spotfinder.backend.v1.deviceManagement.domain.model.queries.GetDevicesByEdgeServerIdQuery;
import com.spotfinder.backend.v1.deviceManagement.domain.model.queries.GetDevicesByParkingIdQuery;
import com.spotfinder.backend.v1.deviceManagement.domain.model.queries.GetUnassignedDevicesByParkingIdQuery;
import com.spotfinder.backend.v1.deviceManagement.domain.services.DeviceQueryService;
import com.spotfinder.backend.v1.deviceManagement.infrastructure.persistence.jpa.repositories.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceQueryServiceImpl implements DeviceQueryService {

    private final DeviceRepository deviceRepository;

    public DeviceQueryServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public List<Device> handle(GetDevicesByParkingIdQuery query) {
        return deviceRepository.findByParkingId_ParkingId(query.parkingId());
    }

    @Override
    public Optional<Device> handle(GetDeviceByParkingSpotIdQuery query) {
        return deviceRepository.findByParkingSpotIdSpotId(query.parkingSpotId());
    }

    @Override
    public List<Device> handle(GetUnassignedDevicesByParkingIdQuery query) {
        return deviceRepository.findByParkingIdParkingIdAndEdgeServerIdEdgeServerId(query.parkingId(), "unassigned");
    }

    @Override
    public List<Device> handle(GetDevicesByEdgeServerIdQuery query) {
        return deviceRepository.findByEdgeServerIdEdgeServerId(query.edgeServerId());
    }
}

package com.spotfinder.backend.v1.deviceManagement.application.internal.commandservices;

import com.spotfinder.backend.v1.deviceManagement.domain.model.aggregates.Device;
import com.spotfinder.backend.v1.deviceManagement.domain.model.commands.CreateDeviceCommand;
import com.spotfinder.backend.v1.deviceManagement.domain.model.commands.UpdateDeviceCommand;
import com.spotfinder.backend.v1.deviceManagement.domain.model.commands.UpdateDeviceMacAddressCommand;
import com.spotfinder.backend.v1.deviceManagement.domain.services.DeviceCommandService;
import com.spotfinder.backend.v1.deviceManagement.infrastructure.gateway.ParkingMqttService;
import com.spotfinder.backend.v1.deviceManagement.infrastructure.persistence.jpa.repositories.DeviceRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class DeviceCommandServiceImpl implements DeviceCommandService {

    private final DeviceRepository deviceRepository;
    private final ParkingMqttService parkingMqttService;

    public DeviceCommandServiceImpl(DeviceRepository deviceRepository, @Lazy ParkingMqttService parkingMqttService) {
        this.deviceRepository = deviceRepository;
        this.parkingMqttService = parkingMqttService;
    }

    @Override
    public Optional<Device> handle(CreateDeviceCommand command) {
        Device device = new Device(command.parkingId(), command.parkingSpotId(), command.spotStatus(), command.spotLabel(), command.edgeServerId());
        return Optional.of(deviceRepository.save(device));
    }

    @Override
    public Optional<Device> handle(UpdateDeviceCommand command) {
        var device = deviceRepository.findByParkingSpotId_SpotId(command.deviceId())
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        device.updateMissingFields(command);
        var updatedDevice = deviceRepository.save(device);
        return Optional.of(updatedDevice);
    }

    @Override
    public Optional<Device> handle(UpdateDeviceMacAddressCommand command) throws IOException {
        var device = deviceRepository.findById(command.deviceId())
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        device.setMacAddress(command.newMacAddress());
        var updatedDevice = deviceRepository.save(device);
        parkingMqttService.sendDeviceListByEdgeServerId(updatedDevice.getEdgeServerId());
        return Optional.of(updatedDevice);
    }
}

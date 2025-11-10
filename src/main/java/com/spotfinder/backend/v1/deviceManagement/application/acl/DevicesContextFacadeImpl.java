package com.spotfinder.backend.v1.deviceManagement.application.acl;

import com.spotfinder.backend.v1.deviceManagement.domain.model.commands.CreateDeviceCommand;
import com.spotfinder.backend.v1.deviceManagement.domain.services.DeviceCommandService;
import com.spotfinder.backend.v1.deviceManagement.interfaces.acl.DevicesContextFacade;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DevicesContextFacadeImpl implements DevicesContextFacade {
    private final DeviceCommandService deviceCommandService;

    public DevicesContextFacadeImpl(DeviceCommandService deviceCommandService) {
        this.deviceCommandService = deviceCommandService;
    }

    @Override
    public void createDevice(Long parkingId, UUID parkingSpotId, String spotStatus, String spotLabel, String edgeServerId) {
        var command = new CreateDeviceCommand(parkingId, parkingSpotId, spotStatus, spotLabel, edgeServerId);
        var device = deviceCommandService.handle(command);
        if (device.isEmpty()) {
            throw new IllegalStateException("Device creation failed");
        }
    }
}

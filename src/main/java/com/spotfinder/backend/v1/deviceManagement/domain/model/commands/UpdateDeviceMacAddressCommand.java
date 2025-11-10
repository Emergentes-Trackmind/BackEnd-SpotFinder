package com.spotfinder.backend.v1.deviceManagement.domain.model.commands;

public record UpdateDeviceMacAddressCommand(
        Long deviceId,
        String newMacAddress
) {
}

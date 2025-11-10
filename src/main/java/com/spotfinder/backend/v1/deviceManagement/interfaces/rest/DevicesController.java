package com.spotfinder.backend.v1.deviceManagement.interfaces.rest;

import com.spotfinder.backend.v1.deviceManagement.domain.model.aggregates.Device;
import com.spotfinder.backend.v1.deviceManagement.domain.model.queries.GetDevicesByEdgeServerIdQuery;
import com.spotfinder.backend.v1.deviceManagement.domain.model.queries.GetUnassignedDevicesByParkingIdQuery;
import com.spotfinder.backend.v1.deviceManagement.domain.services.DeviceCommandService;
import com.spotfinder.backend.v1.deviceManagement.domain.services.DeviceQueryService;
import com.spotfinder.backend.v1.deviceManagement.interfaces.rest.resources.DeviceResource;
import com.spotfinder.backend.v1.deviceManagement.interfaces.rest.resources.UpdateDeviceResource;
import com.spotfinder.backend.v1.deviceManagement.interfaces.rest.transform.DeviceResourceFromEntityAssembler;
import com.spotfinder.backend.v1.deviceManagement.interfaces.rest.transform.UpdateDeviceCommandFromResourceAssembler;
import com.spotfinder.backend.v1.deviceManagement.interfaces.rest.transform.UpdateDeviceMacAddressCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/devices", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Device", description = "Device management")
public class DevicesController {
    private final DeviceCommandService deviceCommandService;
    private final DeviceQueryService deviceQueryService;

    public DevicesController(DeviceCommandService deviceCommandService, DeviceQueryService deviceQueryService) {
        this.deviceCommandService = deviceCommandService;
        this.deviceQueryService = deviceQueryService;
    }

    @Operation(summary = "Update a device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device updated successfully"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @PutMapping("/{deviceId}")
    public ResponseEntity<DeviceResource> updateDevice(@PathVariable UUID deviceId, @RequestBody UpdateDeviceResource deviceResource) {
        Optional<Device> updatedDevice = this.deviceCommandService.handle(
                UpdateDeviceCommandFromResourceAssembler.toCommandFromResource(deviceResource, deviceId));
        if (updatedDevice.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return updatedDevice.map(device ->
                ResponseEntity.ok(DeviceResourceFromEntityAssembler.toResourceFromEntity(device)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update device mac address by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device mac address updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid device id or mac address format"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @PatchMapping("/{deviceId}")
    public ResponseEntity<DeviceResource> updateDeviceMacAddress(@PathVariable Long deviceId, @RequestParam String macAddress) throws IOException {
        Optional<Device> updatedDevice = this.deviceCommandService.handle(
                UpdateDeviceMacAddressCommandFromResourceAssembler.toCommandFromResource(deviceId, macAddress));
        if (updatedDevice.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return updatedDevice.map(device ->
                ResponseEntity.ok(DeviceResourceFromEntityAssembler.toResourceFromEntity(device)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    /*@Operation(summary = "Get devices by parking id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devices retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No devices found for the given parking id")
    })
    @GetMapping("/parking/{parkingId}")
    public ResponseEntity<List<DeviceResource>> getDevicesByParkingId(@PathVariable Long parkingId) {
        var query = new GetDevicesByParkingIdQuery(parkingId);
        List<Device> devices = this.deviceQueryService.handle(query);

        if (devices.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<DeviceResource> resources = devices.stream()
                .map(DeviceResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }*/

    /*@Operation(summary = "Get device by parking spot id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @GetMapping("/parking-spot/{parkingSpotId}")
    public ResponseEntity<DeviceResource> getDeviceByParkingSpotId(@PathVariable String parkingSpotId) {
        var query = new GetDeviceByParkingSpotIdQuery(UUID.fromString(parkingSpotId));
        Optional<Device> device = this.deviceQueryService.handle(query);
        return device.map(source ->
                        ResponseEntity.ok(DeviceResourceFromEntityAssembler.toResourceFromEntity(source)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }*/

    @Operation(summary = "Get devices by edge server id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devices retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No devices found")
    })
    @GetMapping("/edge-server/{edgeServerId}")
    public ResponseEntity<List<DeviceResource>> getDevicesByEdgeServerId(@PathVariable String edgeServerId) {
        var query = new GetDevicesByEdgeServerIdQuery(edgeServerId);
        List<Device> devices = this.deviceQueryService.handle(query);

        if (devices.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<DeviceResource> resources = devices.stream()
                .map(DeviceResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Get unassigned devices by parking id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unassigned devices retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No unassigned devices found for the given parking id")
    })
    @GetMapping("/unassigned/{parkingId}")
    public ResponseEntity<List<DeviceResource>> getUnassignedDevicesByParkingId(@PathVariable Long parkingId) {
        var query = new GetUnassignedDevicesByParkingIdQuery(parkingId);
        List<Device> devices = this.deviceQueryService.handle(query);

        if (devices.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<DeviceResource> resources = devices.stream()
                .map(DeviceResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}

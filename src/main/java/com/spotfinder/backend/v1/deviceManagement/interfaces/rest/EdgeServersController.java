package com.spotfinder.backend.v1.deviceManagement.interfaces.rest;

import com.spotfinder.backend.v1.deviceManagement.domain.model.aggregates.EdgeServer;
import com.spotfinder.backend.v1.deviceManagement.domain.model.queries.GetEdgeServerByParkingIdQuery;
import com.spotfinder.backend.v1.deviceManagement.domain.services.EdgeServerCommandService;
import com.spotfinder.backend.v1.deviceManagement.domain.services.EdgeServerQueryService;
import com.spotfinder.backend.v1.deviceManagement.interfaces.rest.resources.CreateEdgeServerResource;
import com.spotfinder.backend.v1.deviceManagement.interfaces.rest.resources.EdgeServerResource;
import com.spotfinder.backend.v1.deviceManagement.interfaces.rest.transform.CreateEdgeServerCommandFromResourceAssembler;
import com.spotfinder.backend.v1.deviceManagement.interfaces.rest.transform.EdgeServerResourceFromEntityAssembler;
import com.spotfinder.backend.v1.deviceManagement.interfaces.rest.transform.UpdateEdgeServerMacAddressCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/edge-servers", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Edge Server", description = "Edge Server management")
public class EdgeServersController {
    private final EdgeServerCommandService edgeServerCommandService;
    private final EdgeServerQueryService edgeServerQueryService;

    public EdgeServersController(EdgeServerCommandService edgeServerCommandService, EdgeServerQueryService edgeServerQueryService) {
        this.edgeServerCommandService = edgeServerCommandService;
        this.edgeServerQueryService = edgeServerQueryService;
    }

    @Operation(summary = "Create a new edge server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Edge server created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<EdgeServerResource> createEdgeServer(@RequestBody CreateEdgeServerResource resource) {
        Optional<EdgeServer> edgeServer = this.edgeServerCommandService
                .handle(CreateEdgeServerCommandFromResourceAssembler.toCommandFromResource(resource));

        return edgeServer.map(source->
                        new ResponseEntity<>(EdgeServerResourceFromEntityAssembler.toResourceFromEntity(source), CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Get edge servers by parking parkingId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edge servers retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No edge servers found")
    })
    @GetMapping("/parking/{parkingId}")
    public ResponseEntity<List<EdgeServerResource>> getEdgeServersByParkingId(@PathVariable Long parkingId) {
        var query = new GetEdgeServerByParkingIdQuery(parkingId);
        List<EdgeServer> edgeServers = this.edgeServerQueryService.handle(query);
        
        if (edgeServers.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<EdgeServerResource> resources = edgeServers.stream()
                .map(EdgeServerResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Update edge server mac address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edge server updated successfully"),
            @ApiResponse(responseCode = "404", description = "Edge server not found")
    })
    @PatchMapping("/{serverId}")
    public ResponseEntity<EdgeServerResource> updateEdgeServerMacAddress(
            @PathVariable Long serverId, @RequestParam String macAddress) {
        Optional<EdgeServer> updatedEdgeServer = this.edgeServerCommandService.handle(
                UpdateEdgeServerMacAddressCommandFromResourceAssembler.toCommandFromResource(serverId, macAddress)
        );

        return updatedEdgeServer.map(source ->
                        ResponseEntity.ok(EdgeServerResourceFromEntityAssembler.toResourceFromEntity(source)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

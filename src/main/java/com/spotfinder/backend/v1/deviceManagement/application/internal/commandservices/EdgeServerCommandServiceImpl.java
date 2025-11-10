package com.spotfinder.backend.v1.deviceManagement.application.internal.commandservices;

import com.spotfinder.backend.v1.deviceManagement.domain.model.aggregates.EdgeServer;
import com.spotfinder.backend.v1.deviceManagement.domain.model.commands.CreateEdgeServerCommand;
import com.spotfinder.backend.v1.deviceManagement.domain.model.commands.UpdateEdgeServerMacAddressCommand;
import com.spotfinder.backend.v1.deviceManagement.domain.services.EdgeServerCommandService;
import com.spotfinder.backend.v1.deviceManagement.infrastructure.persistence.jpa.repositories.EdgeServerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EdgeServerCommandServiceImpl implements EdgeServerCommandService {

    private final EdgeServerRepository edgeServerRepository;

    public EdgeServerCommandServiceImpl(EdgeServerRepository edgeServerRepository) {
        this.edgeServerRepository = edgeServerRepository;
    }

    @Override
    public Optional<EdgeServer> handle(CreateEdgeServerCommand command) {
        Optional<EdgeServer> existingEdgeServer = edgeServerRepository.findByMacAddress(command.macAddress());
        if (existingEdgeServer.isPresent()) {
            return existingEdgeServer;
        }
        EdgeServer edgeServer = new EdgeServer(command);
        return Optional.of(edgeServerRepository.save(edgeServer));
    }

    @Override
    public Optional<EdgeServer> handle(UpdateEdgeServerMacAddressCommand command) {
        Optional<EdgeServer> existingEdgeServer = edgeServerRepository.findByMacAddress(command.newMacAddress());
        if (existingEdgeServer.isPresent()) {
            return existingEdgeServer;
        }
        Optional<EdgeServer> edgeServerOptional = edgeServerRepository.findById(command.edgeServerId());
        if (edgeServerOptional.isEmpty()) {
            return Optional.empty();
        }
        EdgeServer edgeServer = edgeServerOptional.get();
        edgeServer.setMacAddress(command.newMacAddress());
        return Optional.of(edgeServerRepository.save(edgeServer));
    }
}

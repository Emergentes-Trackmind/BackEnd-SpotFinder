package com.spotfinder.backend.v1.deviceManagement.infrastructure.gateway;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotfinder.backend.v1.deviceManagement.domain.model.aggregates.Device;
import com.spotfinder.backend.v1.deviceManagement.domain.model.aggregates.EdgeServer;
import com.spotfinder.backend.v1.deviceManagement.infrastructure.persistence.jpa.repositories.DeviceRepository;
import com.spotfinder.backend.v1.deviceManagement.infrastructure.persistence.jpa.repositories.EdgeServerRepository;
import com.spotfinder.backend.v1.reservations.application.internal.outboundservices.acl.ExternalParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ParkingMqttService {
    private final MessageChannel mqttOutboundChannel;
    private final ObjectMapper objectMapper;
    @Autowired
    private EdgeServerRepository edgeServerRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private ExternalParkingService externalParkingService;

    public ParkingMqttService(
            @Qualifier("mqttOutboundChannel") MessageChannel mqttOutboundChannel,
            ObjectMapper objectMapper
    ) {
        this.mqttOutboundChannel = mqttOutboundChannel;
        this.objectMapper = objectMapper;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMqttMessage(Message<?> message) {
        try {
            String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);

            // Manejo mejorado del payload
            String payload = extractPayload(message);

            if (topic == null) {
                System.err.println("Topic es null en el mensaje MQTT");
                return;
            }

            System.out.printf("Mensaje recibido - Topic: %s, Payload: %s%n", topic, payload);

            if (topic.matches("parking/.+")) {
                handleParkingMessage(topic, payload);
            } else if ("edge/provisioning".equals(topic)) {
                handleProvisioningMessage(payload);
            } else {
                System.out.println("Topic no manejado: " + topic);
            }
        } catch (Exception e) {
            System.err.printf("Error procesando mensaje MQTT: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractPayload(Message<?> message) throws IOException {
        Object payload = message.getPayload();

        if (payload instanceof String) {
            return (String) payload;
        } else if (payload instanceof byte[]) {
            return new String((byte[]) payload, StandardCharsets.UTF_8);
        } else {
            // Intenta convertir usando ObjectMapper si es otro tipo
            return objectMapper.writeValueAsString(payload);
        }
    }

    private void handleParkingMessage(String topic, String payload) throws IOException {
        String serverId = topic.split("/")[1];
        Map<String, Object> data = objectMapper.readValue(payload, new TypeReference<>() {});

        Boolean occupied = (Boolean) data.get("occupied");

        if (occupied == null) {
            System.out.println("Campo 'occupied' no encontrado en el mensaje: " + payload);
            return;
        }

        String spotId = (String) data.get("spotId");
        String apiKey = (String) data.get("apiKey");

        System.out.printf("Parking ID: %s, Spot: %s, Occupied: %s, ApiKey: %s%n",
                serverId, spotId, occupied, apiKey);

        Long parkingId = edgeServerRepository.findParkingIdByServerId(serverId);

        // Actualizar el estado del parking spot
        externalParkingService.updateParkingSpotAvailability(parkingId, UUID.fromString(spotId), occupied ? "OCCUPIED" : "AVAILABLE");
        externalParkingService.updateAvailableSpotsCount(parkingId, 1, "subtract");
    }

    private void handleProvisioningMessage(String payload) throws IOException {
        Map<String, String> json = objectMapper.readValue(payload, new TypeReference<>() {});
        String mac = json.get("mac");

        System.out.println("Provisioning MAC: " + mac);

        Optional<EdgeServer> edgeServer = edgeServerRepository.findByMacAddress(mac);
        if (edgeServer.isEmpty()) {
            System.out.println("Edge no encontrado para MAC: " + mac);
            return;
        }

        // Enviar configuración del edge
        Map<String, Object> config = loadEdgeConfig(
                edgeServer.get().getParkingId().toString(),
                edgeServer.get().getApiKey(),
                edgeServer.get().getServerId(),
                edgeServer.get().getName());

        sendEdgeConfig(mac, config);

        // Enviar lista de dispositivos por separado
        List<Device> devices = deviceRepository.findAllByEdgeServerId_EdgeServerId(edgeServer.get().getServerId());
        sendDeviceList(mac, devices);
    }

    public void sendEdgeConfig(String mac, Map<String, Object> config) throws IOException {
        String topic = "edge/" + mac;
        byte[] payload = objectMapper.writeValueAsBytes(config);
        mqttOutboundChannel.send(MessageBuilder.withPayload(payload)
                .setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, 1)
                .build());
        System.out.println("Configuración enviada a " + topic);
    }

    public void reserveSpot(String spotId, Boolean reserved) throws IOException {
        Optional<Device> device = deviceRepository.findByParkingSpotId_SpotId(UUID.fromString(spotId));
        if (device.isEmpty()) {
            System.out.println("Dispositivo no encontrado para spotId: " + spotId);
            return;
        }
        String topic = "parking/" + device.get().getEdgeServerId();
        Optional<EdgeServer> edgeServer = edgeServerRepository.findByServerId(device.get().getEdgeServerId());
        if (edgeServer.isEmpty()) {
            System.out.println("Edge server no encontrado para edgeServerId: " + device.get().getEdgeServerId());
            return;
        }
        Map<String, Object> msg = Map.of("spotId", spotId, "apiKey", edgeServer.get().getApiKey(), "reserved", reserved);
        byte[] payload = objectMapper.writeValueAsBytes(msg);
        mqttOutboundChannel.send(MessageBuilder.withPayload(payload)
                .setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, 1)
                .build());
        System.out.println("reserva enviada: " + new String(payload) + " a " + topic);
    }

    private Map<String, Object> loadEdgeConfig(String parkingId, String apiKey, String serverId, String edgeName) {
        return Map.of(
                "type", "config",
                "parkingId", parkingId,
                "apiKey", apiKey,
                "serverId", serverId,
                "edgeName", edgeName
        );
    }

    public void sendDeviceList(String mac, List<Device> devices) throws IOException {
        // Convertir la lista de dispositivos a una estructura más simple para enviar por MQTT
        List<Map<String, String>> deviceList = devices.stream()
                .map(device -> Map.of(
                        "spotId", device.getParkingSpotId(),
                        "spotLabel", device.getSpotLabel(),
                        "status", device.getOperationalStatus(),
                        "macAddress", device.getMacAddress(),
                        "parkingId", String.valueOf(device.getParkingId()),
                        "edgeId", device.getEdgeServerId(),
                        "deviceType", device.getType()
                ))
                .toList();

        Map<String, Object> devicesMessage = Map.of(
                "type", "devices",
                "devices", deviceList
        );

        String topic = "edge/" + mac;
        byte[] payload = objectMapper.writeValueAsBytes(devicesMessage);
        mqttOutboundChannel.send(MessageBuilder.withPayload(payload)
                .setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, 1)
                .build());
        System.out.println("Lista de dispositivos enviada a " + topic + " (" + deviceList.size() + " dispositivos)");
    }

    public void sendDeviceListByEdgeServerId(String edgeServerId) throws IOException {
        Optional<EdgeServer> edgeServer = edgeServerRepository.findByServerId(edgeServerId);
        if (edgeServer.isEmpty()) {
            System.out.println("Edge server no encontrado para serverId: " + edgeServerId);
            return;
        }

        List<Device> devices = deviceRepository.findAllByEdgeServerId_EdgeServerId(edgeServerId);
        sendDeviceList(edgeServer.get().getMacAddress(), devices);
    }
}
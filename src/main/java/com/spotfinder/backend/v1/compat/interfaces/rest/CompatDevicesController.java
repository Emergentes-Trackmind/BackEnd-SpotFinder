package com.spotfinder.backend.v1.compat.interfaces.rest;

import com.spotfinder.backend.v1.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/iot/devices")
@CrossOrigin
@Tag(name = "Compat IoT Devices")
public class CompatDevicesController {

    private final BearerTokenService tokenService;

    public CompatDevicesController(BearerTokenService tokenService) {
        this.tokenService = tokenService;
    }

    public record DeviceJson(String id, String parkingId, String serialNumber, String model,
                             String type, String status, Integer battery, String parkingSpotId,
                             String createdAt, String updatedAt,
                             String parkingName, String parkingSpotLabel,
                             String deviceToken, String mqttTopic, String webhookEndpoint) {}

    public record PagedResponse(List<DeviceJson> data, long total, int page, int size, int totalPages) {}

    private static final Map<String, DeviceJson> STORE = new ConcurrentHashMap<>();
    private static final Map<String, String> SERIAL_INDEX = new ConcurrentHashMap<>();

    private static String nowIso() { return Instant.now().toString(); }

    @GetMapping
    @Operation(summary = "List devices (paged)")
    public ResponseEntity<PagedResponse> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(name = "parking_id", required = false) String parkingId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        var all = new ArrayList<>(STORE.values());
        var filtered = all.stream().filter(d ->
                (type == null || type.equals("all") || type.equalsIgnoreCase(d.type())) &&
                (status == null || status.equals("all") || status.equalsIgnoreCase(d.status())) &&
                (parkingId == null || parkingId.equals("all") || parkingId.equalsIgnoreCase(d.parkingId())) &&
                (q == null || q.isBlank() || d.serialNumber().toLowerCase().contains(q.toLowerCase()))
        ).toList();

        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(filtered.size(), from + size);
        var slice = from >= filtered.size() ? List.<DeviceJson>of() : filtered.subList(from, to);
        long total = filtered.size();
        int totalPages = (int) Math.ceil((double) total / size);
        return ResponseEntity.ok(new PagedResponse(slice, total, page, size, totalPages));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceJson> getById(@PathVariable String id) {
        var d = STORE.get(id);
        if (d == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(d);
    }

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> kpis(@RequestParam(name = "parking_id", required = false) String parkingId) {
        var all = new ArrayList<>(STORE.values());
        if (parkingId != null && !parkingId.equals("all")) {
            all.removeIf(d -> !parkingId.equalsIgnoreCase(d.parkingId()));
        }
        long total = all.size();
        long online = all.stream().filter(d -> "online".equalsIgnoreCase(d.status())).count();
        long offline = all.stream().filter(d -> "offline".equalsIgnoreCase(d.status())).count();
        long maintenance = all.stream().filter(d -> "maintenance".equalsIgnoreCase(d.status())).count();
        var map = new LinkedHashMap<String, Object>();
        map.put("total", total);
        map.put("online", online);
        map.put("offline", offline);
        map.put("maintenance", maintenance);
        return ResponseEntity.ok(map);
    }

    @PostMapping
    public ResponseEntity<DeviceJson> create(@RequestBody Map<String, Object> body) {
        String id = UUID.randomUUID().toString();
        String serial = String.valueOf(body.getOrDefault("serialNumber", id));
        String model = String.valueOf(body.getOrDefault("model", "GENERIC"));
        String type = String.valueOf(body.getOrDefault("type", "sensor"));
        String status = String.valueOf(body.getOrDefault("status", "online"));
        String parkingId = String.valueOf(body.getOrDefault("parkingId", "0"));
        String parkingSpotId = body.get("parkingSpotId") == null ? null : String.valueOf(body.get("parkingSpotId"));
        Integer battery = body.get("battery") == null ? 100 : Integer.parseInt(body.get("battery").toString());
        String now = nowIso();
        var d = new DeviceJson(id, parkingId, serial, model, type, status, battery, parkingSpotId, now, now, null, null, null, null, null);
        STORE.put(id, d);
        SERIAL_INDEX.put(serial, id);
        return ResponseEntity.status(201).body(d);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceJson> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        var d = STORE.get(id);
        if (d == null) return ResponseEntity.notFound().build();
        var updated = new DeviceJson(
                d.id(),
                String.valueOf(body.getOrDefault("parkingId", d.parkingId())),
                String.valueOf(body.getOrDefault("serialNumber", d.serialNumber())),
                String.valueOf(body.getOrDefault("model", d.model())),
                String.valueOf(body.getOrDefault("type", d.type())),
                String.valueOf(body.getOrDefault("status", d.status())),
                body.get("battery") == null ? d.battery() : Integer.parseInt(body.get("battery").toString()),
                body.get("parkingSpotId") == null ? d.parkingSpotId() : String.valueOf(body.get("parkingSpotId")),
                d.createdAt(),
                nowIso(),
                d.parkingName(), d.parkingSpotLabel(), d.deviceToken(), d.mqttTopic(), d.webhookEndpoint()
        );
        STORE.put(id, updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        var d = STORE.remove(id);
        if (d != null) SERIAL_INDEX.remove(d.serialNumber());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/maintenance")
    public ResponseEntity<DeviceJson> maintenance(@PathVariable String id) {
        var d = STORE.get(id);
        if (d == null) return ResponseEntity.notFound().build();
        var updated = new DeviceJson(d.id(), d.parkingId(), d.serialNumber(), d.model(), d.type(), "maintenance", d.battery(), d.parkingSpotId(), d.createdAt(), nowIso(), d.parkingName(), d.parkingSpotLabel(), d.deviceToken(), d.mqttTopic(), d.webhookEndpoint());
        STORE.put(id, updated);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<DeviceJson> restore(@PathVariable String id) {
        var d = STORE.get(id);
        if (d == null) return ResponseEntity.notFound().build();
        var updated = new DeviceJson(d.id(), d.parkingId(), d.serialNumber(), d.model(), d.type(), "online", d.battery(), d.parkingSpotId(), d.createdAt(), nowIso(), d.parkingName(), d.parkingSpotLabel(), d.deviceToken(), d.mqttTopic(), d.webhookEndpoint());
        STORE.put(id, updated);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{serial}/telemetry")
    public ResponseEntity<?> telemetry(@PathVariable String serial, @RequestBody Map<String, Object> body) {
        var id = SERIAL_INDEX.get(serial);
        if (id == null) return ResponseEntity.notFound().build();
        var d = STORE.get(id);
        String status = String.valueOf(body.getOrDefault("status", d.status()));
        Integer battery = body.get("battery") == null ? d.battery() : Integer.parseInt(body.get("battery").toString());
        var updated = new DeviceJson(d.id(), d.parkingId(), d.serialNumber(), d.model(), d.type(), status, battery, d.parkingSpotId(), d.createdAt(), nowIso(), d.parkingName(), d.parkingSpotLabel(), d.deviceToken(), d.mqttTopic(), d.webhookEndpoint());
        STORE.put(id, updated);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{serial}/bind")
    public ResponseEntity<?> bindDevice(@PathVariable String serial,
                                        @RequestBody Map<String, Object> body,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader,
                                        HttpServletRequest request) {
        var id = SERIAL_INDEX.get(serial);
        if (id == null) return ResponseEntity.notFound().build();

        // Autenticación obligatoria vía JWT
        try {
            String bearer = tokenService.getBearerTokenFrom(request);
            if (bearer == null && authHeader != null && authHeader.startsWith("Bearer ")) {
                bearer = authHeader.substring("Bearer ".length());
            }
            if (bearer == null || bearer.isBlank()) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "Unauthorized",
                        "message", "Se requiere token JWT"
                ));
            }
            String userId = tokenService.getUserIdFromToken(bearer);
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "Unauthorized",
                        "message", "Token JWT inválido"
                ));
            }

        String parkingId = String.valueOf(body.getOrDefault("parkingId", "0"));
        String parkingSpotId = body.get("parkingSpotId") == null ? null : String.valueOf(body.get("parkingSpotId"));
        String status = String.valueOf(body.getOrDefault("status", "online"));

        var existing = STORE.get(id);
        if (existing == null) return ResponseEntity.notFound().build();

        var updated = new DeviceJson(
                existing.id(),
                parkingId,
                existing.serialNumber(),
                existing.model(),
                existing.type(),
                status,
                existing.battery(),
                parkingSpotId,
                existing.createdAt(),
                nowIso(),
                existing.parkingName(),
                parkingSpotId != null ? parkingSpotId : existing.parkingSpotLabel(),
                existing.deviceToken(),
                existing.mqttTopic(),
                existing.webhookEndpoint()
        );

        STORE.put(id, updated);

        // Respuesta simple para compatibilidad FE
        return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Unauthorized",
                    "message", "Error de autenticación: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulk(@RequestBody Map<String, Object> body) {
        String parkingId = String.valueOf(body.getOrDefault("parkingId", "0"));
        var devices = (List<Map<String, Object>>) body.getOrDefault("devices", List.of());
        List<DeviceJson> created = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (var item : devices) {
            String serial = String.valueOf(item.getOrDefault("serialNumber", UUID.randomUUID().toString()));
            if (SERIAL_INDEX.containsKey(serial)) {
                warnings.add("Duplicate serial: " + serial);
                continue;
            }
            String id = UUID.randomUUID().toString();
            String model = String.valueOf(item.getOrDefault("model", "GENERIC"));
            String type = String.valueOf(item.getOrDefault("type", "sensor"));
            String spotLabel = String.valueOf(item.getOrDefault("spotLabel", ""));
            String now = nowIso();
            var d = new DeviceJson(id, parkingId, serial, model, type, "online", 100, null, now, now, null, spotLabel, null, null, null);
            STORE.put(id, d);
            SERIAL_INDEX.put(serial, id);
            created.add(d);
        }
        var res = new LinkedHashMap<String, Object>();
        res.put("created", created);
        res.put("warnings", warnings);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{id}/token")
    public ResponseEntity<Map<String, String>> generateToken(@PathVariable String id) {
        var d = STORE.get(id);
        if (d == null) return ResponseEntity.notFound().build();
        String token = UUID.randomUUID().toString();
        String mqttTopic = "spotfinder/devices/" + id;
        String webhook = "/api/iot/devices/" + d.serialNumber() + "/telemetry";
        var updated = new DeviceJson(d.id(), d.parkingId(), d.serialNumber(), d.model(), d.type(), d.status(), d.battery(), d.parkingSpotId(), d.createdAt(), d.updatedAt(), d.parkingName(), d.parkingSpotLabel(), token, mqttTopic, webhook);
        STORE.put(id, updated);
        Map<String, String> body = Map.of(
                "token", token,
                "mqttTopic", mqttTopic,
                "webhookEndpoint", webhook
        );
        return ResponseEntity.ok(body);
    }
}


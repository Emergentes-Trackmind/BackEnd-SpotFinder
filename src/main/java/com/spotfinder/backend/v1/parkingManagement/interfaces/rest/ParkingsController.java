package com.spotfinder.backend.v1.parkingManagement.interfaces.rest;

import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.parkingManagement.domain.model.entities.ParkingSpot;
import com.spotfinder.backend.v1.parkingManagement.domain.model.queries.GetAllParkingQuery;
import com.spotfinder.backend.v1.parkingManagement.domain.model.queries.GetParkingByIdQuery;
import com.spotfinder.backend.v1.parkingManagement.domain.model.queries.GetParkingSpotByIdQuery;
import com.spotfinder.backend.v1.parkingManagement.domain.model.queries.GetParkingSpotsByParkingIdQuery;
import com.spotfinder.backend.v1.parkingManagement.domain.model.queries.GetParkingsByOwnerIdQuery;
import com.spotfinder.backend.v1.parkingManagement.domain.services.ParkingCommandService;
import com.spotfinder.backend.v1.parkingManagement.domain.services.ParkingQueryService;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources.AddParkingSpotResource;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources.CreateParkingResource;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources.ParkingResource;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources.ParkingJson;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.resources.ParkingSpotResource;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.transform.AddParkingSpotCommandFromResourceAssembler;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.transform.CreateParkingCommandFromResourceAssembler;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.transform.ParkingResourceFromEntityAssembler;
import com.spotfinder.backend.v1.parkingManagement.interfaces.rest.transform.ParkingSpotResourceFromEntityAssembler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = {"/api", "/api/v1"}, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Parking", description = "Parking management")
public class ParkingsController {
    private final ParkingCommandService parkingCommandService;
    private final ParkingQueryService parkingQueryService;
    private final com.spotfinder.backend.v1.parkingManagement.infrastructure.persistence.jpa.repositories.ParkingRepository parkingRepository;
    private final com.spotfinder.backend.v1.iam.infrastructure.tokens.jwt.BearerTokenService tokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ParkingsController(ParkingCommandService parkingCommandService,
                              ParkingQueryService parkingQueryService,
                              com.spotfinder.backend.v1.parkingManagement.infrastructure.persistence.jpa.repositories.ParkingRepository parkingRepository,
                              com.spotfinder.backend.v1.iam.infrastructure.tokens.jwt.BearerTokenService tokenService) {
        this.parkingCommandService = parkingCommandService;
        this.parkingQueryService = parkingQueryService;
        this.parkingRepository = parkingRepository;
        this.tokenService = tokenService;
    }

    private ParkingJson toParkingJson(Parking p) {
        String type = p.getType() != null ? p.getType() : "Comercial";
        String status = p.getStatus() != null ? p.getStatus() : "Activo";
        String phone = p.getPhone() != null ? p.getPhone() : "";
        String email = p.getEmail() != null ? p.getEmail() : "";
        String website = p.getWebsite() != null ? p.getWebsite() : "";

        return new ParkingJson(
                String.valueOf(p.getId()),
                String.valueOf(p.getOwnerId()),
                p.getName(),
                type,
                p.getDescription(),
                p.getTotalSpots(),
                p.getAvailableSpots(),
                phone,
                email,
                website,
                status,
                buildLocationMap(p),
                buildPricingMap(p),
                buildFeaturesMap(p)
        );
    }

    private Map<String, Object> buildLocationMap(Parking p) {
        var fallback = new HashMap<String, Object>();
        fallback.put("addressLine", p.getAddress() != null ? p.getAddress() : "");
        fallback.put("city", "");
        fallback.put("postalCode", "");
        fallback.put("state", "");
        fallback.put("country", "");
        fallback.put("latitude", p.getLat() != null ? p.getLat() : 0.0);
        fallback.put("longitude", p.getLng() != null ? p.getLng() : 0.0);
        return safeParse(p.getLocationJson(), fallback);
    }

    private Map<String, Object> buildPricingMap(Parking p) {
        var fallback = new HashMap<String, Object>();
        double hourly = p.getRatePerHour() != null ? p.getRatePerHour() : 0.0;
        fallback.put("hourlyRate", hourly);
        fallback.put("dailyRate", hourly * 24);
        fallback.put("monthlyRate", hourly * 24 * 30);
        fallback.put("currency", "EUR");
        fallback.put("minimumStay", "SinLimite");
        fallback.put("open24h", true);
        var hours = new HashMap<String, Object>();
        hours.put("openTime", "08:00");
        hours.put("closeTime", "22:00");
        fallback.put("operatingHours", hours);
        var days = new HashMap<String, Boolean>();
        days.put("monday", true);
        days.put("tuesday", true);
        days.put("wednesday", true);
        days.put("thursday", true);
        days.put("friday", true);
        days.put("saturday", true);
        days.put("sunday", false);
        fallback.put("operatingDays", days);
        var promotions = new HashMap<String, Boolean>();
        promotions.put("earlyBird", false);
        promotions.put("weekend", false);
        promotions.put("longStay", false);
        fallback.put("promotions", promotions);
        return safeParse(p.getPricingJson(), fallback);
    }

    private Map<String, Object> buildFeaturesMap(Parking p) {
        var fallback = new HashMap<String, Object>();
        fallback.put("security", defaultFlagMap());
        fallback.put("amenities", defaultFlagMap());
        fallback.put("services", defaultFlagMap());
        var payments = defaultFlagMap();
        payments.put("cardPayment", true);
        fallback.put("payments", payments);
        if (p == null) return fallback;
        return safeParse(p.getFeaturesJson(), fallback);
    }

    private Map<String, Boolean> defaultFlagMap() {
        var map = new HashMap<String, Boolean>();
        map.put("security24h", false);
        map.put("cameras", false);
        map.put("lighting", false);
        map.put("accessControl", false);
        map.put("covered", false);
        map.put("elevator", false);
        map.put("bathrooms", false);
        map.put("carWash", false);
        map.put("electricCharging", false);
        map.put("freeWifi", false);
        map.put("valetService", false);
        map.put("maintenance", false);
        map.put("mobilePayment", false);
        map.put("monthlyPasses", false);
        map.put("corporateRates", false);
        return map;
    }

    private Map<String, Object> safeParse(String json, Map<String, Object> fallback) {
        if (json == null || json.isBlank()) return fallback;
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return fallback;
        }
    }

    @Operation(summary = "Create a new parking (FE contract)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Parking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid"),
            @ApiResponse(responseCode = "409", description = "Conflict - Parking name already exists for this owner")
    })
    @PostMapping("/parkings")
    public ResponseEntity<?> createParking(@RequestBody java.util.Map<String, Object> body,
                                                     HttpServletRequest request) {
        // 1) Validar y obtener owner desde JWT
        Long ownerId;
        try {
            String bearer = tokenService.getBearerTokenFrom(request);
            if (bearer == null) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Se requiere token JWT"));
            }
            String sub = tokenService.getUserIdFromToken(bearer);
            if (sub == null) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token JWT invÃ¡lido"));
            }
            ownerId = Long.valueOf(sub);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Error de autenticaciÃ³n: " + e.getMessage()));
        }

        // 2) Validar campos requeridos
        String name = String.valueOf(body.getOrDefault("name", "")).trim();
        if (name.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El nombre del parking es requerido"));
        }

        // 3) Verificar si ya existe un parking con el mismo nombre para este owner
        if (parkingRepository.findByNameAndOwnerIdValue(name, ownerId).isPresent()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                    .body(Map.of("error", "Ya existe un parking con este nombre"));
        }
        String description = String.valueOf(body.getOrDefault("description", ""));

        // Nombres del FE: totalSpaces, accessibleSpaces
        Integer totalSpots = parseInt(body.get("totalSpaces"), 0);
        Integer availableSpots = parseInt(body.get("accessibleSpaces"), 0);
        if (availableSpots > totalSpots) availableSpots = totalSpots;

        // UbicaciÃ³n y precio llegan luego por /locations y /pricing; usar defaults seguros
        String address = String.valueOf(body.getOrDefault("addressLine", ""));
        Double lat = parseDouble(body.get("latitude"), 0.0);
        Double lng = parseDouble(body.get("longitude"), 0.0);
        Float ratePerHour = parseFloat(body.get("hourlyRate"), 0.0f);

        // Calcular una grilla mÃ­nima vÃ¡lida si no viene del FE
        int rows = Math.max(1, (int) Math.round(Math.sqrt(Math.max(1, totalSpots))));
        int cols = Math.max(1, (int) Math.ceil(totalSpots / (double) rows));

        String imageUrl = String.valueOf(body.getOrDefault("imageUrl", "https://placehold.co/600x400?text=Parking"));

        var adjusted = new CreateParkingResource(
                ownerId,
                name,
                description,
                String.valueOf(body.getOrDefault("type", "Comercial")),
                String.valueOf(body.getOrDefault("phone", "")),
                String.valueOf(body.getOrDefault("email", "")),
                String.valueOf(body.getOrDefault("website", "")),
                String.valueOf(body.getOrDefault("status", "Activo")),
                address,
                lat,
                lng,
                ratePerHour,
                totalSpots,
                availableSpots,
                rows,
                cols,
                imageUrl
        );

        Optional<Parking> parking = this.parkingCommandService
                .handle(CreateParkingCommandFromResourceAssembler.toCommandFromResource(adjusted));

        return parking.map(source -> {
                    try {
                        var location = extractLocation(body, source);
                        var pricing = extractPricing(body, source);
                        var features = extractFeatures(body);
                        source.setLocationJson(objectMapper.writeValueAsString(location));
                        source.setPricingJson(objectMapper.writeValueAsString(pricing));
                        source.setFeaturesJson(objectMapper.writeValueAsString(features));
                        parkingRepository.save(source);
                    } catch (Exception ignored) {}
                    return new ResponseEntity<>(toParkingJson(source), CREATED);
                })
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // Helpers de parseo seguros para evitar 400 por tipos incompatibles
    private static Integer parseInt(Object value, Integer defaultValue) {
        try { return value == null ? defaultValue : Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return defaultValue; }
    }
    private static Double parseDouble(Object value, Double defaultValue) {
        try { return value == null ? defaultValue : Double.parseDouble(String.valueOf(value)); } catch (Exception e) { return defaultValue; }
    }
    private static Float parseFloat(Object value, Float defaultValue) {
        try { return value == null ? defaultValue : Float.parseFloat(String.valueOf(value)); } catch (Exception e) { return defaultValue; }
    }

    private Map<String, Object> extractLocation(Map<String, Object> body, Parking p) {
        var location = new HashMap<>(buildLocationMap(p));
        if (body.containsKey("location") && body.get("location") instanceof Map<?, ?> loc) {
            location.putAll((Map<String, Object>) loc);
        }
        if (body.containsKey("addressLine")) location.put("addressLine", String.valueOf(body.get("addressLine")));
        if (body.containsKey("city")) location.put("city", String.valueOf(body.get("city")));
        if (body.containsKey("postalCode")) location.put("postalCode", String.valueOf(body.get("postalCode")));
        if (body.containsKey("state")) location.put("state", String.valueOf(body.get("state")));
        if (body.containsKey("country")) location.put("country", String.valueOf(body.get("country")));
        if (body.containsKey("latitude")) location.put("latitude", parseDouble(body.get("latitude"), 0.0));
        if (body.containsKey("longitude")) location.put("longitude", parseDouble(body.get("longitude"), 0.0));
        return location;
    }

    private Map<String, Object> extractPricing(Map<String, Object> body, Parking p) {
        var pricing = new HashMap<>(buildPricingMap(p));
        if (body.containsKey("pricing") && body.get("pricing") instanceof Map<?, ?> pr) {
            pricing.putAll((Map<String, Object>) pr);
        }
        if (body.containsKey("hourlyRate")) pricing.put("hourlyRate", parseDouble(body.get("hourlyRate"), 0.0));
        if (body.containsKey("dailyRate")) pricing.put("dailyRate", parseDouble(body.get("dailyRate"), 0.0));
        if (body.containsKey("monthlyRate")) pricing.put("monthlyRate", parseDouble(body.get("monthlyRate"), 0.0));
        if (body.containsKey("currency")) pricing.put("currency", String.valueOf(body.get("currency")));
        if (body.containsKey("minimumStay")) pricing.put("minimumStay", String.valueOf(body.get("minimumStay")));
        if (body.containsKey("open24h")) pricing.put("open24h", Boolean.parseBoolean(String.valueOf(body.get("open24h"))));
        if (body.containsKey("operatingHours") && body.get("operatingHours") instanceof Map<?, ?> hours) {
            pricing.put("operatingHours", new HashMap<>((Map<String, Object>) hours));
        }
        if (body.containsKey("operatingDays") && body.get("operatingDays") instanceof Map<?, ?> days) {
            pricing.put("operatingDays", new HashMap<>((Map<String, Object>) days));
        }
        if (body.containsKey("promotions") && body.get("promotions") instanceof Map<?, ?> promos) {
            pricing.put("promotions", new HashMap<>((Map<String, Object>) promos));
        }
        return pricing;
    }

    private Map<String, Object> extractFeatures(Map<String, Object> body) {
        var features = new HashMap<>(buildFeaturesMap(null));
        if (body.containsKey("features") && body.get("features") instanceof Map<?, ?> ft) {
            mergeFeatures(features, (Map<String, Object>) ft);
        }
        return features;
    }

    private void mergeFeatures(Map<String, Object> base, Map<String, Object> incoming) {
        incoming.forEach((key, value) -> {
            if (value instanceof Map<?, ?> valueMap) {
                var target = base.getOrDefault(key, new HashMap<String, Object>());
                if (target instanceof Map<?, ?> tMap) {
                    var copy = new HashMap<String, Object>((Map<String, Object>) tMap);
                    copy.putAll((Map<String, Object>) valueMap);
                    base.put(key, copy);
                } else {
                    base.put(key, new HashMap<>((Map<String, Object>) valueMap));
                }
            } else {
                base.put(key, value);
            }
        });
    }

    @Operation(summary = "Add a parking spot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Parking spot added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/parkings/{parkingId}/spots")
    public ResponseEntity<ParkingSpotResource> addParkingSpot(@PathVariable Long parkingId,
                                                              @RequestBody AddParkingSpotResource resource) {
        Optional<ParkingSpot> parkingSpot = this.parkingCommandService
                .handle(AddParkingSpotCommandFromResourceAssembler.toCommandFromResource(resource, parkingId));

        return parkingSpot.map(source ->
                        new ResponseEntity<>(ParkingSpotResourceFromEntityAssembler.toResourceFromEntity(source), CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Get all parking spots by parking id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parkings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Parking not found")
    })
    @GetMapping("/parkings/{parkingId}/spots")
    public ResponseEntity<List<ParkingSpotResource>> getParkingSpotsByParkingId(@PathVariable Long parkingId) {
        var query = new GetParkingSpotsByParkingIdQuery(parkingId);
        var spots = this.parkingQueryService.handle(query);

        if (spots.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var parkingSpotResources = spots.stream()
                .map(ParkingSpotResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(parkingSpotResources);
    }

    @Operation(summary = "Update parking spot status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Spot status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
            @ApiResponse(responseCode = "404", description = "Spot not found")
    })
    @PatchMapping("/parkings/{parkingId}/spots/{spotId}")
    public ResponseEntity<?> updateSpotStatus(@PathVariable Long parkingId,
                                               @PathVariable String spotId,
                                               @RequestBody Map<String, Object> body,
                                               HttpServletRequest request) {
        try {
            // Validar autenticación y obtener el usuario actual
            String bearer = tokenService.getBearerTokenFrom(request);
            if (bearer == null || bearer.isEmpty()) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token de autenticación requerido"));
            }

            String sub = tokenService.getUserIdFromToken(bearer);
            Long currentUserId = Long.parseLong(sub);

            // Verificar que el parking exista y el usuario sea el propietario
            var parkingOpt = parkingQueryService.handle(new GetParkingByIdQuery(parkingId));
            if (parkingOpt.isEmpty()) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Parking no encontrado"));
            }

            var parking = parkingOpt.get();
            if (!parking.getOwnerId().equals(currentUserId)) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No tienes permisos para modificar este parking"));
            }

            // Buscar el spot por su UUID
            UUID spotUuid;
            try {
                spotUuid = UUID.fromString(spotId);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "ID de spot inválido"));
            }

            var query = new GetParkingSpotByIdQuery(spotUuid, parkingId);
            var spotOpt = parkingQueryService.handle(query);

            if (spotOpt.isEmpty()) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Spot no encontrado"));
            }

            var spot = spotOpt.get();

            // Actualizar el estado si se proporciona
            if (body.containsKey("status")) {
                String newStatus = body.get("status").toString().toUpperCase();
                
                // Validar que el estado sea válido
                try {
                    com.spotfinder.backend.v1.parkingManagement.domain.model.valueobjects.ParkingSpotStatus.valueOf(newStatus);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Estado inválido. Valores permitidos: AVAILABLE, OCCUPIED, RESERVED"));
                }

                spot.updateStatus(newStatus);
                parkingRepository.save(parking);

                // Retornar el spot actualizado
                var resource = ParkingSpotResourceFromEntityAssembler.toResourceFromEntity(spot);
                return ResponseEntity.ok(resource);
            }

            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Se requiere el campo 'status'"));

        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar el spot: " + e.getMessage()));
        }
    }

    @GetMapping("/parkings/{parkingId}")
    @Operation(summary = "Get parking by id (FE contract)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parking returned"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ParkingJson> getParkingById(@PathVariable Long parkingId) {
        var query = new GetParkingByIdQuery(parkingId);
        var parking = this.parkingQueryService.handle(query);

        return parking.map(source ->
                        new ResponseEntity<>(toParkingJson(source), OK))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ===== PATCH / DELETE compatibles con el frontend =====
    @PatchMapping("/parkings/{id}")
    public ResponseEntity<?> patchParking(@PathVariable Long id,
                                                        @RequestBody Map<String, Object> body,
                                                        HttpServletRequest request) {
        var opt = parkingQueryService.handle(new GetParkingByIdQuery(id));
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var p = opt.get();
        
        // Validar propiedad usando JWT
        try {
            String bearer = tokenService.getBearerTokenFrom(request);
            if (bearer != null) {
                String sub = tokenService.getUserIdFromToken(bearer);
                Long currentUserId = Long.parseLong(sub);
                if (!p.getOwnerId().equals(currentUserId)) {
                    return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "No tienes permisos para editar este parking"));
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Error de autenticación: " + e.getMessage()));
        }

        try {
            var currentLocation = extractLocation(body, p);
            var currentPricing = extractPricing(body, p);
            var currentFeatures = buildFeaturesMap(p);

            // Actualizar campos básicos con validación
            if (body.containsKey("name")) {
                String name = String.valueOf(body.get("name")).trim();
                if (name.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "El nombre no puede estar vacío"));
                }
                p.setName(name);
            }
            if (body.containsKey("description")) {
                String description = String.valueOf(body.get("description")).trim();
                if (description.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "La descripción no puede estar vacía"));
                }
                p.setDescription(description);
            }
            if (body.containsKey("type")) p.setType(String.valueOf(body.get("type")).trim());
            if (body.containsKey("phone")) p.setPhone(String.valueOf(body.get("phone")).trim());
            if (body.containsKey("email")) p.setEmail(String.valueOf(body.get("email")).trim());
            if (body.containsKey("website")) p.setWebsite(String.valueOf(body.get("website")).trim());
            if (body.containsKey("status")) p.setStatus(String.valueOf(body.get("status")).trim());

            // Actualizar campos numéricos con validación
            if (body.containsKey("totalSpaces")) {
                int totalSpots = Integer.parseInt(body.get("totalSpaces").toString());
                if (totalSpots < 0) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "El número total de espacios no puede ser negativo"));
                }
                p.setTotalSpots(totalSpots);
            }

            if (body.containsKey("accessibleSpaces")) {
                int availableSpots = Integer.parseInt(body.get("accessibleSpaces").toString());
                if (availableSpots < 0) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "El número de espacios disponibles no puede ser negativo"));
                }
                if (availableSpots > p.getTotalSpots()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "El número de espacios disponibles no puede ser mayor al total"));
                }
                p.setAvailableSpots(availableSpots);
            }

            if (body.containsKey("ratePerHour")) {
                float rate = Float.parseFloat(body.get("ratePerHour").toString());
                if (rate < 0) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "La tarifa por hora no puede ser negativa"));
                }
                p.setRatePerHour(rate);
                currentPricing.put("hourlyRate", (double) rate);
            }

            // Actualizar ubicación con validación
            if (body.containsKey("addressLine")) {
                String address = String.valueOf(body.get("addressLine")).trim();
                if (address.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "La dirección no puede estar vacía"));
                }
                p.setAddress(address);
                currentLocation.put("addressLine", address);
            }
            if (body.containsKey("latitude")) {
                try {
                    Double lat = Double.parseDouble(body.get("latitude").toString());
                    if (lat < -90 || lat > 90) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", "La latitud debe estar entre -90 y 90"));
                    }
                    p.setLat(lat);
                    currentLocation.put("latitude", lat);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Formato de latitud inválido"));
                }
            }
            if (body.containsKey("longitude")) {
                try {
                    Double lng = Double.parseDouble(body.get("longitude").toString());
                    if (lng < -180 || lng > 180) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", "La longitud debe estar entre -180 y 180"));
                    }
                    p.setLng(lng);
                    currentLocation.put("longitude", lng);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Formato de longitud inválido"));
                }
            }

            // Merge de objetos completos si llegan embebidos
            if (body.containsKey("location") && body.get("location") instanceof Map<?, ?> locObj) {
                currentLocation.putAll((Map<String, Object>) locObj);
            }
            if (body.containsKey("pricing") && body.get("pricing") instanceof Map<?, ?> pricingObj) {
                currentPricing.putAll((Map<String, Object>) pricingObj);
            }
            if (body.containsKey("features") && body.get("features") instanceof Map<?, ?> featuresObj) {
                mergeFeatures(currentFeatures, (Map<String, Object>) featuresObj);
            }

            p.setLocationJson(objectMapper.writeValueAsString(currentLocation));
            p.setPricingJson(objectMapper.writeValueAsString(currentPricing));
            p.setFeaturesJson(objectMapper.writeValueAsString(currentFeatures));

            parkingRepository.save(p);
            return ResponseEntity.ok(toParkingJson(p));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error en el formato de datos numéricos: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @DeleteMapping("/parkings/{id}")
    public ResponseEntity<Void> deleteParking(@PathVariable Long id) {
        if (!parkingRepository.existsById(id)) return ResponseEntity.notFound().build();
        parkingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Nuevo listado compatible: ownerId opcional como request param
    @Operation(summary = "List parkings (optional ownerId filter) - FE contract")
    @GetMapping("/parkings")
    public ResponseEntity<List<ParkingJson>> listParkings(@org.springframework.web.bind.annotation.RequestParam(required = false) Long ownerId) {
        List<Parking> parkingList;
        if (ownerId != null) {
            var query = new GetParkingsByOwnerIdQuery(ownerId);
            parkingList = this.parkingQueryService.handle(query);
        } else {
            parkingList = this.parkingQueryService.handle(new GetAllParkingQuery());
        }
        var resources = parkingList.stream()
                .map(this::toParkingJson)
                .toList();
        return ResponseEntity.ok(resources);
    }

    // ====== Locations / Pricing / Features (compat with FE) ======
    public record LocationJson(String id, String profileId, String addressLine, String city, String postalCode,
                               String state, String country, double latitude, double longitude) {}

    public record PricingJson(String id, String profileId, double hourlyRate, Double dailyRate, Double monthlyRate,
                              String currency, String minimumStay, boolean open24h, java.util.Map<String, Object> operatingHours,
                              java.util.Map<String, Boolean> operatingDays, java.util.Map<String, Boolean> promotions) {}

    public record FeaturesJson(String id, String profileId, java.util.Map<String, Boolean> security,
                               java.util.Map<String, Boolean> amenities, java.util.Map<String, Boolean> services,
                               java.util.Map<String, Boolean> payments) {}

    @GetMapping("/locations")
    public ResponseEntity<java.util.List<LocationJson>> getLocations(@RequestParam String profileId) {
        var opt = parkingQueryService.handle(new GetParkingByIdQuery(Long.parseLong(profileId)));
        if (opt.isEmpty()) return ResponseEntity.ok(java.util.List.of());
        var p = opt.get();
        var loc = new LocationJson(String.valueOf(p.getId()), String.valueOf(p.getId()), p.getAddress(), "", "", "", "",
                p.getLat() != null ? p.getLat() : 0.0, p.getLng() != null ? p.getLng() : 0.0);
        return ResponseEntity.ok(java.util.List.of(loc));
    }

    @PostMapping("/locations")
    public ResponseEntity<LocationJson> createLocation(@RequestBody LocationJson body) {
        try {
            if (body.profileId() == null) {
                return ResponseEntity.badRequest()
                    .body(null);
            }

            var opt = parkingQueryService.handle(new GetParkingByIdQuery(Long.parseLong(body.profileId())));
            if (opt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var p = opt.get();

            // ValidaciÃ³n de campos obligatorios
            if (body.addressLine() == null || body.addressLine().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(null);
            }

            // ValidaciÃ³n de coordenadas
            if (body.latitude() < -90 || body.latitude() > 90 || body.longitude() < -180 || body.longitude() > 180) {
                return ResponseEntity.badRequest()
                    .body(null);
            }

            // Actualizar la entidad
            p.setAddress(body.addressLine().trim());
            p.setLat(body.latitude());
            p.setLng(body.longitude());
            
            // Guardar los cambios
            parkingRepository.save(p);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(new LocationJson(
                String.valueOf(p.getId()), String.valueOf(p.getId()), body.addressLine(), body.city(), body.postalCode(), body.state(), body.country(), body.latitude(), body.longitude()
        ));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new LocationJson(
                null, body.profileId(), null, null, null, null, null, 0.0, 0.0
            ));
        }
    }

    @PatchMapping("/locations/{id}")
    public ResponseEntity<LocationJson> updateLocation(@PathVariable String id, @RequestBody java.util.Map<String, Object> body) {
        var pid = Long.parseLong(id);
        var opt = parkingQueryService.handle(new GetParkingByIdQuery(pid));
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var p = opt.get();
        try {
            if (body.containsKey("addressLine")) {
                var f = com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking.class.getDeclaredField("address"); f.setAccessible(true); f.set(p, String.valueOf(body.get("addressLine")));
            }
            if (body.containsKey("latitude")) {
                var f = com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking.class.getDeclaredField("lat"); f.setAccessible(true); f.set(p, Double.parseDouble(body.get("latitude").toString()));
            }
            if (body.containsKey("longitude")) {
                var f = com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking.class.getDeclaredField("lng"); f.setAccessible(true); f.set(p, Double.parseDouble(body.get("longitude").toString()));
            }
        } catch (Exception ignored) {}
        parkingRepository.save(p);
        var loc = new LocationJson(String.valueOf(p.getId()), String.valueOf(p.getId()),
                body.getOrDefault("addressLine", "").toString(),
                String.valueOf(body.getOrDefault("city", "")),
                String.valueOf(body.getOrDefault("postalCode", "")),
                String.valueOf(body.getOrDefault("state", "")),
                String.valueOf(body.getOrDefault("country", "")),
                body.containsKey("latitude") ? Double.parseDouble(body.get("latitude").toString()) : (p.getLat()!=null?p.getLat():0.0),
                body.containsKey("longitude") ? Double.parseDouble(body.get("longitude").toString()) : (p.getLng()!=null?p.getLng():0.0)
        );
        return ResponseEntity.ok(loc);
    }

    @GetMapping("/pricing")
    public ResponseEntity<java.util.List<PricingJson>> getPricing(@RequestParam String profileId) {
        var opt = parkingQueryService.handle(new GetParkingByIdQuery(Long.parseLong(profileId)));
        if (opt.isEmpty()) return ResponseEntity.ok(java.util.List.of());
        var p = opt.get();
        var pricing = new PricingJson(String.valueOf(p.getId()), String.valueOf(p.getId()),
                p.getRatePerHour() != null ? p.getRatePerHour() : 0.0,
                null, null, "USD", "1h", true, null, null, null);
        return ResponseEntity.ok(java.util.List.of(pricing));
    }

    @PostMapping("/pricing")
    public ResponseEntity<PricingJson> createPricing(@RequestBody PricingJson body) {
        try {
            if (body.profileId() == null) {
                return ResponseEntity.badRequest().body(null);
            }

            var opt = parkingQueryService.handle(new GetParkingByIdQuery(Long.parseLong(body.profileId())));
            if (opt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var p = opt.get();

            // Validaciones de tarifas
            if (body.hourlyRate() < 0 || 
                (body.dailyRate() != null && body.dailyRate() < 0) || 
                (body.monthlyRate() != null && body.monthlyRate() < 0)) {
                return ResponseEntity.badRequest().body(null);
            }

            // Actualizar la tarifa por hora
            p.setRatePerHour((float) body.hourlyRate());
            
            // Guardar los cambios
            parkingRepository.save(p);
            
            // Crear respuesta con valores por defecto para campos opcionales
            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(new PricingJson(
                String.valueOf(p.getId()), 
                String.valueOf(p.getId()),
                body.hourlyRate(),
                body.dailyRate() != null ? body.dailyRate() : body.hourlyRate() * 24,
                body.monthlyRate() != null ? body.monthlyRate() : body.hourlyRate() * 24 * 30,
                body.currency() != null ? body.currency() : "USD",
                body.minimumStay() != null ? body.minimumStay() : "1h",
                body.open24h(),
                body.operatingHours() != null ? body.operatingHours() : new java.util.HashMap<>(),
                body.operatingDays() != null ? body.operatingDays() : createDefaultOperatingDays(),
                body.promotions() != null ? body.promotions() : new java.util.HashMap<>()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private java.util.Map<String, Boolean> createDefaultOperatingDays() {
        var days = new java.util.HashMap<String, Boolean>();
        days.put("monday", true);
        days.put("tuesday", true);
        days.put("wednesday", true);
        days.put("thursday", true);
        days.put("friday", true);
        days.put("saturday", true);
        days.put("sunday", true);
        return days;
    }
    

    @PatchMapping("/pricing/{id}")
    public ResponseEntity<PricingJson> updatePricing(@PathVariable String id, @RequestBody java.util.Map<String, Object> body) {
        var pid = Long.parseLong(id);
        var opt = parkingQueryService.handle(new GetParkingByIdQuery(pid));
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var p = opt.get();
        if (body.containsKey("hourlyRate")) {
            p.setRatePerHour(Float.parseFloat(body.get("hourlyRate").toString()));
        }
        parkingRepository.save(p);
        var pricing = new PricingJson(String.valueOf(p.getId()), String.valueOf(p.getId()),
                p.getRatePerHour()!=null?p.getRatePerHour():0.0,
                body.containsKey("dailyRate") ? Double.parseDouble(body.get("dailyRate").toString()) : null,
                body.containsKey("monthlyRate") ? Double.parseDouble(body.get("monthlyRate").toString()) : null,
                String.valueOf(body.getOrDefault("currency", "USD")),
                String.valueOf(body.getOrDefault("minimumStay", "1h")),
                Boolean.parseBoolean(String.valueOf(body.getOrDefault("open24h", true))),
                null, null, null);
        return ResponseEntity.ok(pricing);
    }

    @GetMapping("/features")
    public ResponseEntity<java.util.List<FeaturesJson>> getFeatures(@RequestParam String profileId) {
        return ResponseEntity.ok(java.util.List.of());
    }

    @PostMapping("/features")
    public ResponseEntity<FeaturesJson> createFeatures(@RequestBody FeaturesJson body) {
        try {
            if (body.profileId() == null) {
                return ResponseEntity.badRequest().body(null);
            }

            var opt = parkingQueryService.handle(new GetParkingByIdQuery(Long.parseLong(body.profileId())));
            if (opt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var p = opt.get();
            
            // Validar y procesar las caracterÃ­sticas
            if (body.security() == null || body.amenities() == null || 
                body.services() == null || body.payments() == null) {
                return ResponseEntity.badRequest().body(null);
            }

            // Crear el objeto de respuesta
            var features = new FeaturesJson(
                String.valueOf(p.getId()),
                String.valueOf(p.getId()),
                body.security(),
                body.amenities(),
                body.services(),
                body.payments()
            );

            return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(features);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PatchMapping("/features/{id}")
    public ResponseEntity<FeaturesJson> updateFeatures(@PathVariable String id, @RequestBody FeaturesJson body) {
        try {
            var parkingId = Long.parseLong(id);
            var opt = parkingQueryService.handle(new GetParkingByIdQuery(parkingId));
            if (opt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var p = opt.get();

            // Validar y actualizar las caracterÃ­sticas
            if (body.security() == null || body.amenities() == null || 
                body.services() == null || body.payments() == null) {
                return ResponseEntity.badRequest().body(null);
            }

            // Crear el objeto de respuesta actualizado
            var features = new FeaturesJson(
                id,
                String.valueOf(p.getId()),
                body.security(),
                body.amenities(),
                body.services(),
                body.payments()
            );

            return ResponseEntity.ok(features);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    }




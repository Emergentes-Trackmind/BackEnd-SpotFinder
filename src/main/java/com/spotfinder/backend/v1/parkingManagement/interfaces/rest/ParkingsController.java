package com.spotfinder.backend.v1.parkingManagement.interfaces.rest;

import com.spotfinder.backend.v1.parkingManagement.domain.model.aggregates.Parking;
import com.spotfinder.backend.v1.parkingManagement.domain.model.entities.ParkingSpot;
import com.spotfinder.backend.v1.parkingManagement.domain.model.queries.GetAllParkingQuery;
import com.spotfinder.backend.v1.parkingManagement.domain.model.queries.GetParkingByIdQuery;
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
                status
        );
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
                        .body(Map.of("error", "Token JWT inválido"));
            }
            ownerId = Long.valueOf(sub);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Error de autenticación: " + e.getMessage()));
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

        // Ubicación y precio llegan luego por /locations y /pricing; usar defaults seguros
        String address = String.valueOf(body.getOrDefault("addressLine", ""));
        Double lat = parseDouble(body.get("latitude"), 0.0);
        Double lng = parseDouble(body.get("longitude"), 0.0);
        Float ratePerHour = parseFloat(body.get("hourlyRate"), 0.0f);

        // Calcular una grilla mínima válida si no viene del FE
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

        return parking.map(source ->
                        new ResponseEntity<>(toParkingJson(source), CREATED))
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
            }

            // Actualizar ubicación
            // Actualizar ubicación con validación
            if (body.containsKey("addressLine")) {
                String address = String.valueOf(body.get("addressLine")).trim();
                if (address.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "La dirección no puede estar vacía"));
                }
                p.setAddress(address);
            }
            if (body.containsKey("latitude")) {
                try {
                    Double lat = Double.parseDouble(body.get("latitude").toString());
                    if (lat < -90 || lat > 90) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", "La latitud debe estar entre -90 y 90"));
                    }
                    p.setLat(lat);
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
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Formato de longitud inválido"));
                }
            }

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

            // Validación de campos obligatorios
            if (body.addressLine() == null || body.addressLine().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(null);
            }

            // Validación de coordenadas
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
            
            // Validar y procesar las características
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

            // Validar y actualizar las características
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


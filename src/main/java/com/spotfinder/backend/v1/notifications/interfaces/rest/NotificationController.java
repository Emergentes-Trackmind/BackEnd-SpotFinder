package com.spotfinder.backend.v1.notifications.interfaces.rest;

import com.spotfinder.backend.v1.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.spotfinder.backend.v1.notifications.application.service.NotificationService;
import com.spotfinder.backend.v1.notifications.domain.model.FcmToken;
import com.spotfinder.backend.v1.notifications.domain.repository.FcmTokenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping({"/api/notifications", "/api/v1/notifications"})
@Tag(name = "Notifications", description = "Notification Management Endpoints")
public class NotificationController {

    // ===== DTOs =====
    public record AppNotification(
            String id,
            String title,
            String body,
            String kind,
            boolean read,
            String createdAt,
            String actionLabel,
            String actionUrl,
            Map<String, Object> metadata
    ) {}

    public record NotificationListResponse(List<AppNotification> data, long total, long unreadCount, int page, int size) {}

    public record RegisterTokenRequest(String token) {}

    public record SendNotificationRequest(String userId, String title, String body, String kind,
                                          Boolean sendEmail, String actionLabel, String actionUrl, Map<String, Object> metadata) {}

    private final BearerTokenService tokenService;
    private final FcmTokenRepository tokenRepository;
    private final NotificationService notificationService;

    // In-memory store per userId (String) para la UI
    private static final Map<String, List<AppNotification>> STORE = new ConcurrentHashMap<>();

    public NotificationController(BearerTokenService tokenService,
                                  FcmTokenRepository tokenRepository,
                                  NotificationService notificationService) {
        this.tokenService = tokenService;
        this.tokenRepository = tokenRepository;
        this.notificationService = notificationService;
    }

    private String nowIso() { return Instant.now().toString(); }

    private String getUserId(HttpServletRequest request) {
        try {
            String token = tokenService.getBearerTokenFrom(request);
            if (token == null) return "0";
            String sub = tokenService.getUserIdFromToken(token);
            return sub != null ? sub : "0";
        } catch (Exception e) { return "0"; }
    }

    private List<AppNotification> getUserList(String userId) {
        return STORE.computeIfAbsent(userId, k -> new ArrayList<>());
    }

    private long countUnread(List<AppNotification> list) {
        return list.stream().filter(n -> !n.read()).count();
    }

    private static String genId() { return "notif_" + UUID.randomUUID(); }

    // ===== GET /api/notifications (listado con filtros) =====
    @GetMapping
    @Operation(summary = "List notifications (current user)")
    public ResponseEntity<NotificationListResponse> list(HttpServletRequest request,
                                                         @RequestParam(required = false) String q,
                                                         @RequestParam(required = false) Boolean read,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        String userId = getUserId(request);
        var all = new ArrayList<>(getUserList(userId));

        if (q != null && !q.isBlank()) {
            String qq = q.toLowerCase();
            all.removeIf(n -> !(n.title().toLowerCase().contains(qq) || n.body().toLowerCase().contains(qq)));
        }
        if (read != null) {
            all.removeIf(n -> n.read() != read);
        }

        long total = all.size();
        long unread = countUnread(getUserList(userId));

        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(all.size(), from + size);
        var slice = from >= all.size() ? List.<AppNotification>of() : all.subList(from, to);
        return ResponseEntity.ok(new NotificationListResponse(slice, total, unread, page, size));
    }

    // ===== PATCH /{id}/read =====
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markRead(HttpServletRequest request, @PathVariable String id) {
        String userId = getUserId(request);
        var list = getUserList(userId);
        for (int i = 0; i < list.size(); i++) {
            var n = list.get(i);
            if (n.id().equals(id)) {
                list.set(i, new AppNotification(n.id(), n.title(), n.body(), n.kind(), true, n.createdAt(), n.actionLabel(), n.actionUrl(), n.metadata()));
                break;
            }
        }
        return ResponseEntity.ok().build();
    }

    // ===== PATCH /read-all =====
    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllRead(HttpServletRequest request) {
        String userId = getUserId(request);
        var list = getUserList(userId);
        for (int i = 0; i < list.size(); i++) {
            var n = list.get(i);
            if (!n.read()) list.set(i, new AppNotification(n.id(), n.title(), n.body(), n.kind(), true, n.createdAt(), n.actionLabel(), n.actionUrl(), n.metadata()));
        }
        return ResponseEntity.ok().build();
    }

    // ===== DELETE /{id} =====
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification by id")
    public ResponseEntity<Void> deleteOne(HttpServletRequest request, @PathVariable String id) {
        String userId = getUserId(request);
        var list = getUserList(userId);
        list.removeIf(n -> n.id().equals(id));
        return ResponseEntity.noContent().build();
    }

    // ===== DELETE (all) =====
    @DeleteMapping
    @Operation(summary = "Delete all notifications (current user)")
    public ResponseEntity<Void> deleteAll(HttpServletRequest request) {
        String userId = getUserId(request);
        getUserList(userId).clear();
        return ResponseEntity.noContent().build();
    }

    // ===== GET /unread-count =====
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count")
    public ResponseEntity<Map<String, Long>> unreadCount(HttpServletRequest request) {
        String userId = getUserId(request);
        long count = countUnread(getUserList(userId));
        return ResponseEntity.ok(Map.of("count", count));
    }

    // ===== POST /register-fcm-token =====
    @PostMapping("/register-fcm-token")
    @Operation(summary = "Register FCM token for current user")
    public ResponseEntity<Void> registerToken(HttpServletRequest request, @RequestBody RegisterTokenRequest body) {
        String userId = getUserId(request);
        if (body == null || body.token() == null || body.token().isBlank()) return ResponseEntity.badRequest().build();

        var existingByToken = tokenRepository.findByToken(body.token());
        if (existingByToken.isPresent()) {
            var token = existingByToken.get();
            if (!Objects.equals(token.getUserId(), Long.valueOf(userId))) {
                token.setUserId(Long.valueOf(userId));
                tokenRepository.save(token);
            }
        } else {
            FcmToken token = new FcmToken();
            token.setUserId(Long.valueOf(userId));
            token.setToken(body.token());
            tokenRepository.save(token);
        }
        return ResponseEntity.ok().build();
    }

    // ===== POST /send =====
    @PostMapping("/send")
    @Operation(summary = "Send a notification to a user (store + push)")
    public ResponseEntity<Void> send(HttpServletRequest request, @RequestBody SendNotificationRequest body) {
        String currentUserId = getUserId(request);
        String targetUserId = (body.userId() != null && !body.userId().isBlank()) ? body.userId() : currentUserId;

        var list = getUserList(targetUserId);
        var notif = new AppNotification(
                genId(),
                body.title(),
                body.body(),
                body.kind() != null ? body.kind() : "info",
                false,
                nowIso(),
                body.actionLabel(),
                body.actionUrl(),
                body.metadata() != null ? body.metadata() : Map.of()
        );
        list.add(0, notif);

        try {
            List<FcmToken> tokens = tokenRepository.findAllByUserId(Long.valueOf(targetUserId));
            for (var t : tokens) {
                notificationService.sendNotification(t.getToken(), body.title(), body.body());
            }
        } catch (Exception ignored) {}

        return ResponseEntity.ok().build();
    }
}

package com.cattle.notifications.web;

import com.cattle.config.LambdaContext;
import com.cattle.enums.LogType;
import com.cattle.notifications.dto.NotificationDTO;
import com.cattle.notifications.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Campana de notificaciones de una finca.
 * Hereda la seguridad de {@code /farms/**} configurada en {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/farms/{farmId}/notifications")
@Tag(name = "Notificaciones", description = "Campana in-app: listado, conteo de no leídas y marcado de lectura")
public class NotificationController {

    private static final int DEFAULT_LIMIT = 30;
    private static final int MAX_LIMIT = 100;
    private static final String STATUS_UNREAD = "unread";

    private final NotificationService notificationService;
    private final LambdaContext lambdaContext;

    public NotificationController(NotificationService notificationService, LambdaContext lambdaContext) {
        this.notificationService = notificationService;
        this.lambdaContext = lambdaContext;
    }

    @Operation(summary = "Listar notificaciones de la finca",
            description = "Devuelve las notificaciones más recientes primero. status=unread filtra las no leídas.")
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> list(
            @Parameter(description = "ID de la finca", required = true) @PathVariable("farmId") String farmId,
            @Parameter(description = "Máximo de resultados (1-100)") @RequestParam(value = "limit", defaultValue = "30") int limit,
            @Parameter(description = "all | unread") @RequestParam(value = "status", defaultValue = "all") String status) {
        boolean unreadOnly = STATUS_UNREAD.equalsIgnoreCase(status);
        lambdaContext.logInfo(LogType.CONTROLLER, "Listing notifications. farmId=" + farmId + ", unreadOnly=" + unreadOnly);
        return ResponseEntity.ok(notificationService.listByFarm(farmId, clampLimit(limit), unreadOnly));
    }

    @Operation(summary = "Conteo de notificaciones no leídas", description = "Para el badge de la campana.")
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(
            @Parameter(description = "ID de la finca", required = true) @PathVariable("farmId") String farmId) {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(farmId)));
    }

    @Operation(summary = "Marcar una notificación como leída")
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markRead(
            @Parameter(description = "ID de la finca", required = true) @PathVariable("farmId") String farmId,
            @Parameter(description = "ID de la notificación", required = true) @PathVariable("notificationId") String notificationId) {
        lambdaContext.logInfo(LogType.CONTROLLER, "Marking notification read. farmId=" + farmId + ", notificationId=" + notificationId);
        notificationService.markRead(farmId, notificationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Marcar todas las notificaciones como leídas")
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllRead(
            @Parameter(description = "ID de la finca", required = true) @PathVariable("farmId") String farmId) {
        int updated = notificationService.markAllRead(farmId);
        lambdaContext.logInfo(LogType.CONTROLLER, "Marked all notifications read. farmId=" + farmId + ", updated=" + updated);
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    private int clampLimit(int requested) {
        if (requested < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(requested, MAX_LIMIT);
    }
}

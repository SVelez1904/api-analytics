package com.innovatech.api_analytics.controller;

import com.innovatech.api_analytics.service.AnaliticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
public class AnaliticaController {

    private final AnaliticaService analyticsService;

    // Endpoint existente para las barras de progreso y totales
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }

    // 🔥 NUEVO ENDPOINT: Para ver la distribución del personal asignado
    @GetMapping("/workload")
    public ResponseEntity<Map<String, Object>> getWorkload() {
        try {
            Map<String, Object> workloadData = analyticsService.getCargaTrabajoUsuarios();
            return ResponseEntity.ok(workloadData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al calcular carga de trabajo: " + e.getMessage()));
        }
    }
}
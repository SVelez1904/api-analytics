package com.innovatech.api_analytics.controller;

import com.innovatech.api_analytics.service.AnaliticaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AnaliticaController.class)
class AnaliticaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnaliticaService analyticsService;

    // --- 1. TESTS PARA /api/analytics/dashboard/stats ---

    @Test
    @DisplayName("GET /stats - Debe retornar 200 y las estadísticas globales del dashboard")
    void getStats_Exitoso() throws Exception {
        // Arrange
        Map<String, Object> mockStats = Map.of(
                "totalProyectos", 5L,
                "promedioProgreso", 82.3
        );
        when(analyticsService.getDashboardStats()).thenReturn(mockStats);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProyectos").value(5))
                .andExpect(jsonPath("$.promedioProgreso").value(82.3));
    }

    // --- 2. TESTS PARA /api/analytics/dashboard/workload ---

    @Test
    @DisplayName("GET /workload - Debe retornar 200 y la distribución de carga de trabajo")
    void getWorkload_Exitoso() throws Exception {
        // Arrange
        Map<String, Object> mockWorkload = Map.of(
                "usuariosAsignadosAProyectos", 3L,
                "detalleCargaTrabajo", List.of(Map.of("usuarioId", 1, "cantidadProyectos", 2))
        );
        when(analyticsService.getCargaTrabajoUsuarios()).thenReturn(mockWorkload);

        // Act & Assert
        mockMvc.perform(get("/api/analytics/dashboard/workload")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuariosAsignadosAProyectos").value(3))
                .andExpect(jsonPath("$.detalleCargaTrabajo[0].usuarioId").value(1));
    }

    @Test
    @DisplayName("GET /workload - Debe retornar 500 si el servicio lanza una excepción interna")
    void getWorkload_ErrorServidor() throws Exception {
        // Arrange
        when(analyticsService.getCargaTrabajoUsuarios())
                .thenThrow(new RuntimeException("Fallo en la conexión interna de base de datos"));

        // Act & Assert
        mockMvc.perform(get("/api/analytics/dashboard/workload")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error al calcular carga de trabajo: Fallo en la conexión interna de base de datos"));
    }
}
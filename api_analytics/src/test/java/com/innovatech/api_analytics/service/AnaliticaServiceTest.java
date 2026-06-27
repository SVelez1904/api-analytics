package com.innovatech.api_analytics.service;

import com.innovatech.api_analytics.repository.ProyectoMetricaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnaliticaServiceTest {

    @Mock
    private ProyectoMetricaRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AnaliticaService analiticaService;

    // --- 1. TESTS PARA getDashboardStats ---

    @Test
    @DisplayName("getDashboardStats - Debe retornar mapa con estadísticas completas")
    void getDashboardStats_Exitoso() {
        // Arrange
        when(repository.count()).thenReturn(5L);
        when(repository.getPromedioProgresoGeneral()).thenReturn(75.5);

        List<Object[]> mockEstados = List.of(
                new Object[]{"EN_PROGRESO", 3L},
                new Object[]{"COMPLETADO", 2L},
                new Object[]{null, 1L} // Probamos el caso nulo "SIN_ESTADO"
        );
        when(repository.countProyectosByEstado()).thenReturn(mockEstados);

        // Act
        Map<String, Object> stats = analiticaService.getDashboardStats();

        // Assert
        assertEquals(5L, stats.get("totalProyectos"));
        assertEquals(75.5, stats.get("promedioProgreso"));

        @SuppressWarnings("unchecked")
        Map<String, Long> distribucion = (Map<String, Long>) stats.get("distribucionEstados");
        assertNotNull(distribucion);
        assertEquals(3L, distribucion.get("EN_PROGRESO"));
        assertEquals(2L, distribucion.get("COMPLETADO"));
        assertEquals(1L, distribucion.get("SIN_ESTADO"));
    }

    @Test
    @DisplayName("getDashboardStats - Debe manejar promedios nulos y dejarlos en 0.0")
    void getDashboardStats_PromedioNulo() {
        when(repository.count()).thenReturn(0L);
        when(repository.getPromedioProgresoGeneral()).thenReturn(null);
        when(repository.countProyectosByEstado()).thenReturn(List.of());

        Map<String, Object> stats = analiticaService.getDashboardStats();

        assertEquals(0.0, stats.get("promedioProgreso"));
    }

    // --- 2. TESTS PARA getCargaTrabajoUsuarios ---

    @Test
    @DisplayName("getCargaTrabajoUsuarios - Caso Exitoso con API Usuarios en línea")
    @SuppressWarnings("unchecked")
    void getCargaTrabajoUsuarios_Exitoso() {
        // Arrange
        List<Object[]> rawWorkload = List.of(
                new Object[]{101L, 3L},
                new Object[]{102L, 1L}
        );
        when(repository.countProyectosPorUsuario()).thenReturn(rawWorkload);
        when(repository.countUsuariosActivos()).thenReturn(2L);

        // Mock de llamada HTTP síncrona exitosa (Retorna 10 usuarios en el sistema)
        when(restTemplate.getForObject("http://api-usuarios:8080/usuarios/count", Long.class))
                .thenReturn(10L);

        // Act
        Map<String, Object> stats = analiticaService.getCargaTrabajoUsuarios();

        // Assert
        assertEquals(2L, stats.get("usuariosAsignadosAProyectos"));
        assertEquals(10L, stats.get("totalUsuariosRegistrados"));
        assertEquals(8L, stats.get("usuariosSinProyecto")); // 10 registrados - 2 activos = 8 sin proyecto

        List<Map<String, Object>> detalle = (List<Map<String, Object>>) stats.get("detalleCargaTrabajo");
        assertEquals(2, detalle.size());
        assertEquals(101L, detalle.get(0).get("usuarioId"));
        assertEquals(3L, detalle.get(0).get("cantidadProyectos"));
    }

    @Test
    @DisplayName("getCargaTrabajoUsuarios - Debe manejar respuesta nula de API Usuarios")
    void getCargaTrabajoUsuarios_ApiRetornaNulo() {
        when(repository.countProyectosPorUsuario()).thenReturn(new ArrayList<>());
        when(repository.countUsuariosActivos()).thenReturn(3L);
        when(restTemplate.getForObject("http://api-usuarios:8080/usuarios/count", Long.class))
                .thenReturn(null);

        Map<String, Object> stats = analiticaService.getCargaTrabajoUsuarios();

        assertEquals(0, stats.get("usuariosSinProyecto"));
        assertEquals(3L, stats.get("totalUsuariosRegistrados"));
    }

    @Test
    @DisplayName("getCargaTrabajoUsuarios - Resiliencia cuando API Usuarios está fuera de línea (Lanza Excepción)")
    void getCargaTrabajoUsuarios_ApiFueraDeLinea() {
        // Arrange
        when(repository.countProyectosPorUsuario()).thenReturn(new ArrayList<>());
        when(repository.countUsuariosActivos()).thenReturn(5L);

        // Forzamos a que el RestTemplate tire error simulando timeout o caída del contenedor
        when(restTemplate.getForObject("http://api-usuarios:8080/usuarios/count", Long.class))
                .thenThrow(new RuntimeException("Connection timed out"));

        // Act
        Map<String, Object> stats = analiticaService.getCargaTrabajoUsuarios();

        // Assert - Validamos que no explote y que devuelva el string controlado
        assertEquals("No disponible (API Usuarios fuera de línea)", stats.get("usuariosSinProyecto"));
        assertEquals("Desconocido", stats.get("totalUsuariosRegistrados"));
    }
}
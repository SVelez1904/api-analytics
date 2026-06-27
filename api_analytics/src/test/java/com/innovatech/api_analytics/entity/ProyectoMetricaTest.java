package com.innovatech.api_analytics.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProyectoMetricaTest {

    @Test
    @DisplayName("ProyectoMetrica - Debe funcionar correctamente el Patrón Builder y los Getters")
    void testProyectoMetricaBuilderYGetters() {
        // Arrange
        LocalDateTime ahora = LocalDateTime.now();
        Set<Long> usuarios = Set.of(101L, 102L);

        // Act
        ProyectoMetrica metrica = ProyectoMetrica.builder()
                .proyectoId(1L)
                .nombreProyecto("Plataforma Innovatech")
                .progreso(85.5)
                .estado("EN_PROGRESO")
                .javaUltimaActualizacion(ahora)
                .usuarioIds(usuarios)
                .build();

        // Assert
        assertEquals(1L, metrica.getProyectoId());
        assertEquals("Plataforma Innovatech", metrica.getNombreProyecto());
        assertEquals(85.5, metrica.getProgreso());
        assertEquals("EN_PROGRESO", metrica.getEstado());
        assertEquals(ahora, metrica.getJavaUltimaActualizacion());
        assertEquals(2, metrica.getUsuarioIds().size());
        assertTrue(metrica.getUsuarioIds().contains(101L));
    }

    @Test
    @DisplayName("ProyectoMetrica - Debe funcionar el Constructor Vacío y los Setters")
    void testProyectoMetricaSettersYNoArgsConstructor() {
        // Arrange
        ProyectoMetrica metrica = new ProyectoMetrica();
        LocalDateTime ahora = LocalDateTime.now();
        Set<Long> usuarios = new HashSet<>();
        usuarios.add(500L);

        // Act
        metrica.setProyectoId(2L);
        metrica.setNombreProyecto("API Analytics");
        metrica.setProgreso(100.0);
        metrica.setEstado("COMPLETADO");
        metrica.setJavaUltimaActualizacion(ahora);
        metrica.setUsuarioIds(usuarios);

        // Assert
        assertEquals(2L, metrica.getProyectoId());
        assertEquals("API Analytics", metrica.getNombreProyecto());
        assertEquals(100.0, metrica.getProgreso());
        assertEquals("COMPLETADO", metrica.getEstado());
        assertEquals(ahora, metrica.getJavaUltimaActualizacion());
        assertEquals(1, metrica.getUsuarioIds().size());
    }

    @Test
    @DisplayName("ProyectoMetrica - Debe verificar los métodos de soporte Equals, HashCode y ToString")
    void testEqualsHashCodeToString() {
        LocalDateTime ahora = LocalDateTime.now();

        ProyectoMetrica m1 = new ProyectoMetrica(1L, "Test", 50.0, "ACTIVO", ahora, Set.of(1L));
        ProyectoMetrica m2 = new ProyectoMetrica(1L, "Test", 50.0, "ACTIVO", ahora, Set.of(1L));
        ProyectoMetrica m3 = new ProyectoMetrica(2L, "Otro", 10.0, "PAUSADO", ahora, Set.of(2L));

        // Verificar Equals y HashCode
        assertEquals(m1, m2);
        assertNotEquals(m1, m3);
        assertEquals(m1.hashCode(), m2.hashCode());

        // Verificar ToString (evita sorpresas con colecciones perezosas o nulas)
        String toStringResult = m1.toString();
        assertTrue(toStringResult.contains("proyectoId=1"));
        assertTrue(toStringResult.contains("nombreProyecto=Test"));
    }
}
package com.innovatech.api_analytics.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProyectoEventTest {

    @Test
    @DisplayName("ProyectoEvent - Debe verificar el funcionamiento de Getters, Setters y Constructor Vacío")
    void testGettersSettersYConstructorVacio() {
        // Arrange
        ProyectoEvent evento = new ProyectoEvent();
        List<Long> ids = List.of(201L, 202L);

        // Act
        evento.setId(10L);
        evento.setNombre("Migración Kafka");
        evento.setProgresoPorcentaje(45);
        evento.setEstado("EN_PROGRESO");
        evento.setAction("UPDATE");
        evento.setUsuarioIds(ids);

        // Assert
        assertEquals(10L, evento.getId());
        assertEquals("Migración Kafka", evento.getNombre());
        assertEquals(45, evento.getProgresoPorcentaje());
        assertEquals("EN_PROGRESO", evento.getEstado());
        assertEquals("UPDATE", evento.getAction());
        assertEquals(2, evento.getUsuarioIds().size());
        assertEquals(201L, evento.getUsuarioIds().get(0));
    }

    @Test
    @DisplayName("ProyectoEvent - Debe verificar el Constructor con todos los campos")
    void testAllArgsConstructor() {
        // Act
        List<Long> ids = List.of(301L);
        ProyectoEvent evento = new ProyectoEvent(5L, "Nuevo Frontend", 0, "PLANIFICADO", "CREATE", ids);

        // Assert
        assertEquals(5L, evento.getId());
        assertEquals("Nuevo Frontend", evento.getNombre());
        assertEquals(0, evento.getProgresoPorcentaje());
        assertEquals("PLANIFICADO", evento.getEstado());
        assertEquals("CREATE", evento.getAction());
        assertEquals(ids, evento.getUsuarioIds());
    }

    @Test
    @DisplayName("ProyectoEvent - Debe validar Equals, HashCode y ToString de Lombok")
    void testEqualsHashCodeToString() {
        // Arrange
        List<Long> usuariosA = List.of(1L, 2L);
        List<Long> usuariosB = List.of(1L, 2L);

        ProyectoEvent evento1 = new ProyectoEvent(1L, "Event", 50, "OK", "SYNC", usuariosA);
        ProyectoEvent evento2 = new ProyectoEvent(1L, "Event", 50, "OK", "SYNC", usuariosB);
        ProyectoEvent evento3 = new ProyectoEvent(2L, "Otro", 10, "FAIL", "DELETE", List.of(3L));

        // Assert para Equals y HashCode
        assertEquals(evento1, evento2);
        assertNotEquals(evento1, evento3);
        assertEquals(evento1.hashCode(), evento2.hashCode());

        // Assert para ToString
        String toStringResult = evento1.toString();
        assertTrue(toStringResult.contains("id=1"));
        assertTrue(toStringResult.contains("nombre=Event"));
        assertTrue(toStringResult.contains("action=SYNC"));
    }
}
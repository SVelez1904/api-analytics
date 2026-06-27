package com.innovatech.api_analytics.service;

import com.innovatech.api_analytics.dto.ProyectoEvent;
import com.innovatech.api_analytics.entity.ProyectoMetrica;
import com.innovatech.api_analytics.repository.ProyectoMetricaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnaliticaConsumerTest {

    @Mock
    private ProyectoMetricaRepository repository;

    @InjectMocks
    private AnaliticaConsumer analiticaConsumer;

    // --- 1. TESTS PARA VALIDACIONES INICIALES ---

    @Test
    @DisplayName("procesarEventoProyecto - Debe ignorar el evento si es nulo o no tiene ID")
    void testEventoInvalido() {
        // Evento completamente nulo
        analiticaConsumer.procesarEventoProyecto(null);

        // Evento con ID nulo
        ProyectoEvent eventoIdNulo = new ProyectoEvent();
        analiticaConsumer.procesarEventoProyecto(eventoIdNulo);

        // Verificamos que nunca interactúe con el repositorio
        verifyNoInteractions(repository);
    }

    // --- 2. TESTS PARA FLUJO A: DELETE ---

    @Test
    @DisplayName("DELETE - Debe eliminar el registro si el proyecto existe en la DB")
    void testDelete_ProyectoExiste() {
        ProyectoEvent evento = new ProyectoEvent(1L, "Test", 50, "ACTIVO", "DELETE", List.of(1L));
        when(repository.existsById(1L)).thenReturn(true);

        analiticaConsumer.procesarEventoProyecto(evento);

        verify(repository, times(1)).deleteById(1L);
        verify(repository, times(1)).flush();
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("DELETE - Debe ignorar el borrado si el proyecto no existe en la DB")
    void testDelete_ProyectoNoExiste() {
        ProyectoEvent evento = new ProyectoEvent(2L, "Test", 50, "ACTIVO", "DELETE", List.of(1L));
        when(repository.existsById(2L)).thenReturn(false);

        analiticaConsumer.procesarEventoProyecto(evento);

        verify(repository, never()).deleteById(anyLong());
        verify(repository, never()).flush();
    }

    // --- 3. TESTS PARA FLUJO B: CREATE / UPDATE ---

    @Test
    @DisplayName("CREATE/UPDATE - Debe crear un nuevo registro si el proyecto no existía")
    void testCreate_ProyectoNuevo() {
        // Arrange
        ProyectoEvent evento = new ProyectoEvent(3L, "Nuevo Proyecto", 80, "EN_PROGRESO", "CREATE", List.of(101L, 102L));
        when(repository.findById(3L)).thenReturn(Optional.empty());

        // Act
        analiticaConsumer.procesarEventoProyecto(evento);

        // Assert
        verify(repository, times(1)).saveAndFlush(argThat(metrica -> {
            assertEquals(3L, metrica.getProyectoId());
            assertEquals("Nuevo Proyecto", metrica.getNombreProyecto());
            assertEquals(80.0, metrica.getProgreso());
            assertEquals("EN_PROGRESO", metrica.getEstado());
            assertNotNull(metrica.getJavaUltimaActualizacion());
            assertEquals(2, metrica.getUsuarioIds().size());
            return true;
        }));
    }

    @Test
    @DisplayName("CREATE/UPDATE - Debe actualizar el registro existente limpiando y renovando usuarios")
    void testUpdate_ProyectoExistente() {
        // Arrange
        ProyectoEvent evento = new ProyectoEvent(4L, "Proyecto Editado", null, "COMPLETADO", "UPDATE", List.of(500L));

        ProyectoMetrica metricaExistente = new ProyectoMetrica();
        metricaExistente.setProyectoId(4L);
        metricaExistente.setUsuarioIds(new HashSet<>(Set.of(1L, 2L, 3L))); // Usuarios viejos

        when(repository.findById(4L)).thenReturn(Optional.of(metricaExistente));

        // Act
        analiticaConsumer.procesarEventoProyecto(evento);

        // Assert
        verify(repository, times(1)).saveAndFlush(argThat(metrica -> {
            assertEquals("Proyecto Editado", metrica.getNombreProyecto());
            assertEquals(0.0, metrica.getProgreso()); // Valida que maneje el progreso nulo pasándolo a 0.0
            assertEquals("COMPLETADO", metrica.getEstado());
            assertEquals(1, metrica.getUsuarioIds().size());
            assertTrue(metrica.getUsuarioIds().contains(500L)); // Solo queda el nuevo usuario
            return true;
        }));
    }

    @Test
    @DisplayName("CREATE/UPDATE - Debe manejar correctamente las colecciones si el payload viene con usuarios nulos")
    void testCreate_ConUsuariosNulos() {
        ProyectoEvent evento = new ProyectoEvent(5L, "Proyecto Fantasma", 10, "PLANIFICADO", "CREATE", null);
        when(repository.findById(5L)).thenReturn(Optional.empty());

        analiticaConsumer.procesarEventoProyecto(evento);

        verify(repository, times(1)).saveAndFlush(argThat(metrica -> {
            assertNotNull(metrica.getUsuarioIds());
            assertTrue(metrica.getUsuarioIds().isEmpty());
            return true;
        }));
    }
}
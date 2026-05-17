package com.innovatech.api_analytics.service;

import com.innovatech.api_analytics.dto.ProyectoEvent;
import com.innovatech.api_analytics.entity.ProyectoMetrica;
import com.innovatech.api_analytics.repository.ProyectoMetricaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class AnaliticaConsumer {

    @Autowired
    private ProyectoMetricaRepository repository;

    @KafkaListener(topics = "proyectos-topic", groupId = "innovatech-analytics-group-v3")
    @Transactional
    public void procesarEventoProyecto(ProyectoEvent evento) {
        if (evento == null || evento.getId() == null) {
            System.err.println("🚨 Evento recibido inválido o con ID nulo.");
            return;
        }

        System.out.println("📥 Recibiendo evento para el proyecto ID: " + evento.getId() + " [Acción: " + evento.getAction() + "]");

        // =========================================================================
        // 🔥 FLUJO A: ELIMINACIÓN DE REGISTROS (DELETE)
        // =========================================================================
        if ("DELETE".toUpperCase().equals(evento.getAction())) {
            if (repository.existsById(evento.getId())) {
                // Gracias a la configuración en cascada de JPA (CascadeType.ALL / orphanRemoval = true),
                // borrar al padre limpiará automáticamente la tabla intermedia 'proyecto_usuarios_analytics'
                repository.deleteById(evento.getId());
                repository.flush(); // Aseguramos el borrado físico síncrono inmediato
                System.out.println("🗑️ Analytics limpiado por completo para el proyecto ID: " + evento.getId());
            } else {
                System.out.println("ℹ️ Intento de DELETE ignorado: El proyecto ID " + evento.getId() + " no existía en analíticas.");
            }
            return; // Cortamos el hilo para evitar procesar inserts de una entidad borrada
        }

        // =========================================================================
        // 🔄 FLUJO B: SINK DE DATOS Y SINCRONIZACIÓN (CREATE / UPDATE)
        // =========================================================================

        // 1. Buscamos la métrica existente o instanciamos una nueva de respaldo
        ProyectoMetrica metrica = repository.findById(evento.getId())
                .orElseGet(() -> {
                    ProyectoMetrica nueva = new ProyectoMetrica();
                    nueva.setProyectoId(evento.getId());
                    nueva.setUsuarioIds(new HashSet<>());
                    return nueva;
                });

        // 2. Mapeo de campos y propiedades primitivas
        metrica.setNombreProyecto(evento.getNombre());
        metrica.setProgreso(evento.getProgresoPorcentaje() != null ? evento.getProgresoPorcentaje().doubleValue() : 0.0);
        metrica.setEstado(evento.getEstado());

        // Descomentado para auditoría de tiempo real en la base de datos de Analytics
        metrica.setJavaUltimaActualizacion(LocalDateTime.now());

        // 3. Sincronización limpia y segura de la colección de IDs de usuarios
        Set<Long> nuevosUsuarios = evento.getUsuarioIds() != null ? new HashSet<>(evento.getUsuarioIds()) : new HashSet<>();

        if (metrica.getUsuarioIds() == null) {
            metrica.setUsuarioIds(new HashSet<>());
        } else {
            metrica.getUsuarioIds().clear(); // Vaciamos para romper las relaciones viejas
        }

        // Inyectamos el listado fresco que viaja en el payload de Kafka
        metrica.getUsuarioIds().addAll(nuevosUsuarios);

        // 4. Único punto de guardado y vaciado (Flush) físico a la base de datos PostgreSQL
        repository.saveAndFlush(metrica);

        System.out.println("🎉 Sincronización exitosa en analíticas para el proyecto: " + evento.getId() + " con " + nuevosUsuarios.size() + " usuarios.");
    }
}
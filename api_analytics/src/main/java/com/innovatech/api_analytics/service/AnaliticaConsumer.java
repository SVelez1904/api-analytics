package com.innovatech.api_analytics.service;

import com.innovatech.api_analytics.dto.ProyectoEvent;
import com.innovatech.api_analytics.entity.ProyectoMetrica;
import com.innovatech.api_analytics.repository.ProyectoMetricaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AnaliticaConsumer {

    @Autowired
    private ProyectoMetricaRepository repository;

    @KafkaListener(topics = "proyectos-topic", groupId = "innovatech-analytics-group")
    public void procesarEventoProyecto(ProyectoEvent evento) {
        System.out.println("Recibiendo evento para el proyecto: " + evento.getNombre());

        ProyectoMetrica metrica = new ProyectoMetrica();
        metrica.setProyectoId(evento.getId());
        metrica.setNombreProyecto(evento.getNombre());
        metrica.setProgreso(evento.getProgresoPorcentaje());
        metrica.setEstado(evento.getEstado());

        metrica.setFechaRegistro(LocalDateTime.now());
        repository.save(metrica);
    }
}
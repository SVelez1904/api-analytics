package com.innovatech.api_analytics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "metricas_proyectos")
public class ProyectoMetrica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long proyectoId;

    @Column(nullable = false)
    private String nombreProyecto;

    private Integer progreso;

    @Column(length = 50)
    private String estado;

    // Se asigna automáticamente al insertar
    @Column(updatable = false)
    private LocalDateTime fechaRegistro;


    private LocalDateTime ultimaActualizacion;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        this.ultimaActualizacion = LocalDateTime.now();
    }
}
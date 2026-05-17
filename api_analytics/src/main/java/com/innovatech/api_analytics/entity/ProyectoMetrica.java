package com.innovatech.api_analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "metricas_proyectos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProyectoMetrica {

    @Id // 🔥 ESTE ES EL PASO CRUCIAL: El ID de Proyectos funciona como nuestra Clave Primaria local. Sin @GeneratedValue.
    @Column(name = "proyecto_id")
    private Long proyectoId;

    private String nombreProyecto;
    private Double progreso;
    private String estado;
    private LocalDateTime javaUltimaActualizacion; // (O el nombre de tu atributo)

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "proyecto_usuarios_analytics",
            joinColumns = @JoinColumn(name = "proyecto_id") // 🔥 Enlaza directamente al @Id de arriba
    )
    @Column(name = "usuario_id")
    private Set<Long> usuarioIds = new HashSet<>();
}
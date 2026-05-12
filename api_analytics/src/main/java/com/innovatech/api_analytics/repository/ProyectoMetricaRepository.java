package com.innovatech.api_analytics.repository;

import com.innovatech.api_analytics.entity.ProyectoMetrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProyectoMetricaRepository extends JpaRepository<ProyectoMetrica, Long> {

    // Ver la evolución de un proyecto específico
    List<ProyectoMetrica> findByProyectoIdOrderByFechaRegistroAsc(Long proyectoId);

    // Obtener el promedio de progreso global
    @Query("SELECT AVG(p.progreso) FROM ProyectoMetrica p WHERE p.fechaRegistro > :fecha")
    Double obtenerPromedioProgresoReciente(@Param("fecha") LocalDateTime fecha);

    // Cuenta cuántos proyectos hay por cada estado
    @Query("SELECT p.estado, COUNT(p) FROM ProyectoMetrica p GROUP BY p.estado")
    List<Object[]> countProyectosByEstado();

    // Promedio de progreso total
    @Query("SELECT AVG(p.progreso) FROM ProyectoMetrica p")
    Double getPromedioProgresoGeneral();
}
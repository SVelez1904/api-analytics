package com.innovatech.api_analytics.repository;

import com.innovatech.api_analytics.entity.ProyectoMetrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProyectoMetricaRepository extends JpaRepository<ProyectoMetrica, Long> {

    // 🔥 SOLUCIÓN: Comentamos este método que busca una propiedad inexistente
    // List<ProyectoMetrica> findByProyectoIdOrderByFechaRegistroAsc(Long proyectoId);

    // Buscador clave para la lógica de Upsert en el Consumer de Kafka
    Optional<ProyectoMetrica> findByProyectoId(Long proyectoId);

    // Necesario para la distribución de barras en React
    @Query("SELECT p.estado, COUNT(p) FROM ProyectoMetrica p GROUP BY p.estado")
    List<Object[]> countProyectosByEstado();

    // Necesario para el KPI de progreso global
    @Query("SELECT AVG(p.progreso) FROM ProyectoMetrica p")
    Double getPromedioProgresoGeneral();

    // Total de usuarios únicos trabajando actualmente
    @Query("SELECT COUNT(DISTINCT u) FROM ProyectoMetrica p JOIN p.usuarioIds u")
    Long countUsuariosActivos();

    // Proyectos por usuario (Devuelve pares de: [ID de Usuario, Cantidad de Proyectos])
    @Query("SELECT u, COUNT(p) FROM ProyectoMetrica p JOIN p.usuarioIds u GROUP BY u")
    List<Object[]> countProyectosPorUsuario();
}
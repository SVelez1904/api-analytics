package com.innovatech.api_analytics.service;

import com.innovatech.api_analytics.repository.ProyectoMetricaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnaliticaService {

    private final ProyectoMetricaRepository repository;
    private final RestTemplate restTemplate; // Se inyecta automáticamente desde RestClientConfig externo

    // --- 1. ENDPOINT: ESTADÍSTICAS GLOBALES DEL DASHBOARD ---
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long total = repository.count();
        stats.put("totalProyectos", total);

        Double promedio = repository.getPromedioProgresoGeneral();
        stats.put("promedioProgreso", promedio != null ? promedio : 0.0);

        Map<String, Long> estados = new HashMap<>();
        repository.countProyectosByEstado().forEach(result -> {
            String estadoKey = result[0] != null ? (String) result[0] : "SIN_ESTADO";
            estados.put(estadoKey, (Long) result[1]);
        });
        stats.put("distribucionEstados", estados);

        return stats;
    }

    // --- 2. NUEVO MÉTODO: GESTIÓN DE CARGA DE TRABAJO (WORKLOAD) ---
    public Map<String, Object> getCargaTrabajoUsuarios() {
        Map<String, Object> stats = new HashMap<>();

        // A. Obtener pares de [ID_USUARIO, CANTIDAD_PROYECTOS] de la DB local
        List<Object[]> resultadosRaw = repository.countProyectosPorUsuario();

        // Mapeamos a una estructura JSON clave-valor para coordinar con el front
        List<Map<String, Object>> proyectosPorUsuario = resultadosRaw.stream().map(row -> {
            Map<String, Object> userStat = new HashMap<>();
            userStat.put("usuarioId", row[0]);
            userStat.put("cantidadProyectos", row[1]);
            return userStat;
        }).toList();
        stats.put("detalleCargaTrabajo", proyectosPorUsuario);

        // B. Total de usuarios únicos metidos en algún proyecto actualmente
        Long usuariosActivos = repository.countUsuariosActivos();
        long activosCount = usuariosActivos != null ? usuariosActivos : 0L;
        stats.put("usuariosAsignadosAProyectos", activosCount);

        // C. Comunicación síncrona vía HTTP con api-usuarios para deducir los desocupados
        try {
            // Llamamos al endpoint de conteo global de api-usuarios usando el puerto interno de Docker
            Long totalUsuariosSistema = restTemplate.getForObject("http://api-usuarios:8080/usuarios/count", Long.class);
            if (totalUsuariosSistema != null) {
                long usuariosSinProyecto = totalUsuariosSistema - activosCount;
                stats.put("usuariosSinProyecto", Math.max(0, usuariosSinProyecto));
                stats.put("totalUsuariosRegistrados", totalUsuariosSistema);
            } else {
                stats.put("usuariosSinProyecto", 0);
                stats.put("totalUsuariosRegistrados", activosCount);
            }
        } catch (Exception e) {
            // Si api-usuarios está caído o no responde, el dashboard no se rompe catastróficamente
            stats.put("usuariosSinProyecto", "No disponible (API Usuarios fuera de línea)");
            stats.put("totalUsuariosRegistrados", "Desconocido");
        }

        return stats;
    }
}
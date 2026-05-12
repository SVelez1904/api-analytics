package com.innovatech.api_analytics.service;

import com.innovatech.api_analytics.repository.ProyectoMetricaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnaliticaService {

    private final ProyectoMetricaRepository repository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalProyectos", repository.count());
        stats.put("promedioProgreso", repository.getPromedioProgresoGeneral());

        // Convertimos la lista de estados en un Map
        Map<String, Long> estados = new HashMap<>();
        repository.countProyectosByEstado().forEach(result -> {
            estados.put((String) result[0], (Long) result[1]);
        });
        stats.put("distribucionEstados", estados);

        return stats;
    }
}

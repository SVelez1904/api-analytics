package com.innovatech.api_analytics.controller;

import com.innovatech.api_analytics.service.AnaliticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics/dashboard")
@RequiredArgsConstructor
public class AnaliticaController {

    private final AnaliticaService analyticsService;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return analyticsService.getDashboardStats();
    }
}
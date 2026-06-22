package org.example.lab_equipment_repair.controller;

import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.ApiResponse;
import org.example.lab_equipment_repair.dto.DashboardStatsResponse;
import org.example.lab_equipment_repair.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardStatsResponse> dashboardStats() {
        return ApiResponse.success(statisticsService.dashboardStats());
    }
}

package org.example.lab_equipment_repair.dto;

public record DashboardStatsResponse(
        Long submittedCount,
        Long inProgressCount,
        Long repairedCount,
        Long closedCount,
        Long equipmentCount
) {
}

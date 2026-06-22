package org.example.lab_equipment_repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.dto.DashboardStatsResponse;
import org.example.lab_equipment_repair.entity.Equipment;
import org.example.lab_equipment_repair.entity.RepairOrder;
import org.example.lab_equipment_repair.enums.OrderStatus;
import org.example.lab_equipment_repair.mapper.EquipmentMapper;
import org.example.lab_equipment_repair.mapper.RepairOrderMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final RepairOrderMapper repairOrderMapper;

    private final EquipmentMapper equipmentMapper;

    public DashboardStatsResponse dashboardStats() {
        return new DashboardStatsResponse(
                countOrder(OrderStatus.SUBMITTED),
                countOrder(OrderStatus.IN_PROGRESS),
                countOrder(OrderStatus.REPAIRED),
                countOrder(OrderStatus.CLOSED),
                equipmentMapper.selectCount(new LambdaQueryWrapper<Equipment>())
        );
    }

    private Long countOrder(OrderStatus status) {
        return repairOrderMapper.selectCount(new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getStatus, status.name()));
    }
}

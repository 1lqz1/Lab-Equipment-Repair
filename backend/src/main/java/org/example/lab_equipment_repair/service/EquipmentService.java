package org.example.lab_equipment_repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.BusinessException;
import org.example.lab_equipment_repair.dto.EquipmentRequest;
import org.example.lab_equipment_repair.entity.Equipment;
import org.example.lab_equipment_repair.enums.EquipmentStatus;
import org.example.lab_equipment_repair.mapper.EquipmentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentMapper equipmentMapper;

    public List<Equipment> listEquipment(Long labId, String status, String keyword) {
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<Equipment>()
                .eq(labId != null, Equipment::getLabId, labId)
                .eq(status != null && !status.isBlank(), Equipment::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), query -> query
                        .like(Equipment::getName, keyword)
                        .or()
                        .like(Equipment::getCode, keyword))
                .orderByDesc(Equipment::getCreatedAt);
        return equipmentMapper.selectList(wrapper);
    }

    public Equipment createEquipment(EquipmentRequest request) {
        Long count = equipmentMapper.selectCount(new LambdaQueryWrapper<Equipment>().eq(Equipment::getCode, request.getCode()));
        if (count > 0) {
            throw new BusinessException("设备编号已存在");
        }
        Equipment equipment = new Equipment();
        equipment.setCode(request.getCode());
        equipment.setName(request.getName());
        equipment.setCategory(request.getCategory());
        equipment.setLabId(request.getLabId());
        equipment.setStatus(request.getStatus() == null || request.getStatus().isBlank()
                ? EquipmentStatus.NORMAL.name()
                : request.getStatus());
        equipment.setResponsibleUserId(request.getResponsibleUserId());
        equipment.setPurchaseDate(request.getPurchaseDate());
        equipmentMapper.insert(equipment);
        return equipment;
    }
}

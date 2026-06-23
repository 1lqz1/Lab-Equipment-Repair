package org.example.lab_equipment_repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.BusinessException;
import org.example.lab_equipment_repair.dto.EquipmentRequest;
import org.example.lab_equipment_repair.entity.Equipment;
import org.example.lab_equipment_repair.entity.Lab;
import org.example.lab_equipment_repair.entity.RepairOrder;
import org.example.lab_equipment_repair.enums.EquipmentStatus;
import org.example.lab_equipment_repair.mapper.EquipmentMapper;
import org.example.lab_equipment_repair.mapper.LabMapper;
import org.example.lab_equipment_repair.mapper.RepairOrderMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentMapper equipmentMapper;
    private final LabMapper labMapper;
    private final RepairOrderMapper repairOrderMapper;

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
        ensureCodeAvailable(null, request.getCode());
        ensureLabUsable(request.getLabId());
        Equipment equipment = new Equipment();
        fillEquipment(equipment, request);
        equipmentMapper.insert(equipment);
        return equipment;
    }

    public Equipment updateEquipment(Long id, EquipmentRequest request) {
        Equipment equipment = getById(id);
        ensureCodeAvailable(id, request.getCode());
        ensureLabUsable(request.getLabId());
        fillEquipment(equipment, request);
        equipmentMapper.updateById(equipment);
        return equipment;
    }

    public Equipment updateStatus(Long id, String status) {
        Equipment equipment = getById(id);
        EquipmentStatus parsedStatus = parseStatus(status);
        equipment.setStatus(parsedStatus.name());
        equipmentMapper.updateById(equipment);
        return equipment;
    }

    public void deleteEquipment(Long id) {
        Equipment equipment = getById(id);
        Long orderCount = repairOrderMapper.selectCount(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getEquipmentId, id));
        if (orderCount > 0) {
            throw new BusinessException("设备已有工单引用，不能删除，可改为停用或报废");
        }
        equipmentMapper.deleteById(equipment.getId());
    }

    private void fillEquipment(Equipment equipment, EquipmentRequest request) {
        equipment.setCode(request.getCode().trim());
        equipment.setName(request.getName().trim());
        equipment.setCategory(normalize(request.getCategory()));
        equipment.setLabId(request.getLabId());
        equipment.setStatus(parseStatus(request.getStatus() == null || request.getStatus().isBlank()
                ? EquipmentStatus.NORMAL.name()
                : request.getStatus()).name());
        equipment.setResponsibleUserId(request.getResponsibleUserId());
        equipment.setPurchaseDate(request.getPurchaseDate());
    }

    private Equipment getById(Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            throw new BusinessException("设备不存在");
        }
        return equipment;
    }

    private void ensureCodeAvailable(Long currentId, String code) {
        Equipment exists = equipmentMapper.selectOne(new LambdaQueryWrapper<Equipment>().eq(Equipment::getCode, code.trim()));
        if (exists != null && !exists.getId().equals(currentId)) {
            throw new BusinessException("设备编号已存在");
        }
    }

    private void ensureLabUsable(Long labId) {
        Lab lab = labMapper.selectById(labId);
        if (lab == null) {
            throw new BusinessException("实验室不存在");
        }
        if ("DISABLED".equals(lab.getStatus())) {
            throw new BusinessException("实验室已停用，不能绑定设备");
        }
    }

    private EquipmentStatus parseStatus(String status) {
        try {
            return EquipmentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("设备状态不合法");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

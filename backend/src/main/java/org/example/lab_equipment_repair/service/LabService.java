package org.example.lab_equipment_repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.BusinessException;
import org.example.lab_equipment_repair.dto.LabRequest;
import org.example.lab_equipment_repair.entity.Equipment;
import org.example.lab_equipment_repair.entity.Lab;
import org.example.lab_equipment_repair.entity.RepairOrder;
import org.example.lab_equipment_repair.mapper.EquipmentMapper;
import org.example.lab_equipment_repair.mapper.LabMapper;
import org.example.lab_equipment_repair.mapper.RepairOrderMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabService {

    private final LabMapper labMapper;
    private final EquipmentMapper equipmentMapper;
    private final RepairOrderMapper repairOrderMapper;

    public List<Lab> listLabs(String status, String keyword) {
        LambdaQueryWrapper<Lab> wrapper = new LambdaQueryWrapper<Lab>()
                .eq(status != null && !status.isBlank(), Lab::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), query -> query
                        .like(Lab::getName, keyword)
                        .or()
                        .like(Lab::getLocation, keyword)
                        .or()
                        .like(Lab::getDescription, keyword))
                .orderByDesc(Lab::getCreatedAt);
        return labMapper.selectList(wrapper);
    }

    public Lab createLab(LabRequest request) {
        ensureNameAvailable(null, request.getName());
        Lab lab = new Lab();
        fillLab(lab, request);
        lab.setStatus("ACTIVE");
        labMapper.insert(lab);
        return lab;
    }

    public Lab updateLab(Long id, LabRequest request) {
        Lab lab = getById(id);
        ensureNameAvailable(id, request.getName());
        fillLab(lab, request);
        labMapper.updateById(lab);
        return lab;
    }

    public Lab enableLab(Long id) {
        Lab lab = getById(id);
        lab.setStatus("ACTIVE");
        labMapper.updateById(lab);
        return lab;
    }

    public Lab disableLab(Long id) {
        Lab lab = getById(id);
        lab.setStatus("DISABLED");
        labMapper.updateById(lab);
        return lab;
    }

    public void deleteLab(Long id) {
        getById(id);
        Long equipmentCount = equipmentMapper.selectCount(new LambdaQueryWrapper<Equipment>().eq(Equipment::getLabId, id));
        Long orderCount = repairOrderMapper.selectCount(new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getLabId, id));
        if (equipmentCount > 0 || orderCount > 0) {
            throw new BusinessException("实验室已有设备或工单引用，不能删除，可改为停用");
        }
        labMapper.deleteById(id);
    }

    private void fillLab(Lab lab, LabRequest request) {
        lab.setName(request.getName().trim());
        lab.setLocation(normalize(request.getLocation()));
        lab.setManagerId(request.getManagerId());
        lab.setDescription(normalize(request.getDescription()));
    }

    private Lab getById(Long id) {
        Lab lab = labMapper.selectById(id);
        if (lab == null) {
            throw new BusinessException("实验室不存在");
        }
        return lab;
    }

    private void ensureNameAvailable(Long currentId, String name) {
        Lab exists = labMapper.selectOne(new LambdaQueryWrapper<Lab>().eq(Lab::getName, name.trim()));
        if (exists != null && !exists.getId().equals(currentId)) {
            throw new BusinessException("实验室名称已存在");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

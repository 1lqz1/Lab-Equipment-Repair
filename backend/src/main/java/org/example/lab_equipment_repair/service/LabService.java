package org.example.lab_equipment_repair.service;

import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.dto.LabRequest;
import org.example.lab_equipment_repair.entity.Lab;
import org.example.lab_equipment_repair.mapper.LabMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabService {

    private final LabMapper labMapper;

    public List<Lab> listLabs() {
        return labMapper.selectList(null);
    }

    public Lab createLab(LabRequest request) {
        Lab lab = new Lab();
        lab.setName(request.getName());
        lab.setLocation(request.getLocation());
        lab.setManagerId(request.getManagerId());
        lab.setDescription(request.getDescription());
        labMapper.insert(lab);
        return lab;
    }
}

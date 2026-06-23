package org.example.lab_equipment_repair.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.ApiResponse;
import org.example.lab_equipment_repair.dto.EquipmentRequest;
import org.example.lab_equipment_repair.dto.EquipmentStatusRequest;
import org.example.lab_equipment_repair.entity.Equipment;
import org.example.lab_equipment_repair.service.EquipmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    public ApiResponse<List<Equipment>> listEquipment(
            @RequestParam(required = false) Long labId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(equipmentService.listEquipment(labId, status, keyword));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAB_MANAGER')")
    public ApiResponse<Equipment> createEquipment(@Valid @RequestBody EquipmentRequest request) {
        return ApiResponse.success(equipmentService.createEquipment(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAB_MANAGER')")
    public ApiResponse<Equipment> updateEquipment(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        return ApiResponse.success(equipmentService.updateEquipment(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAB_MANAGER')")
    public ApiResponse<Equipment> updateStatus(@PathVariable Long id, @Valid @RequestBody EquipmentStatusRequest request) {
        return ApiResponse.success(equipmentService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAB_MANAGER')")
    public ApiResponse<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ApiResponse.success();
    }
}

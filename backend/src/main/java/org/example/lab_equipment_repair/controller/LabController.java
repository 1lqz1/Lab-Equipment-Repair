package org.example.lab_equipment_repair.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.ApiResponse;
import org.example.lab_equipment_repair.dto.LabRequest;
import org.example.lab_equipment_repair.entity.Lab;
import org.example.lab_equipment_repair.service.LabService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/labs")
@RequiredArgsConstructor
public class LabController {

    private final LabService labService;

    @GetMapping
    public ApiResponse<List<Lab>> listLabs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(labService.listLabs(status, keyword));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAB_MANAGER')")
    public ApiResponse<Lab> createLab(@Valid @RequestBody LabRequest request) {
        return ApiResponse.success(labService.createLab(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAB_MANAGER')")
    public ApiResponse<Lab> updateLab(@PathVariable Long id, @Valid @RequestBody LabRequest request) {
        return ApiResponse.success(labService.updateLab(id, request));
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAB_MANAGER')")
    public ApiResponse<Lab> enableLab(@PathVariable Long id) {
        return ApiResponse.success(labService.enableLab(id));
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LAB_MANAGER')")
    public ApiResponse<Lab> disableLab(@PathVariable Long id) {
        return ApiResponse.success(labService.disableLab(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteLab(@PathVariable Long id) {
        labService.deleteLab(id);
        return ApiResponse.success();
    }
}

package org.example.lab_equipment_repair.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.ApiResponse;
import org.example.lab_equipment_repair.dto.LabRequest;
import org.example.lab_equipment_repair.entity.Lab;
import org.example.lab_equipment_repair.service.LabService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/labs")
@RequiredArgsConstructor
public class LabController {

    private final LabService labService;

    @GetMapping
    public ApiResponse<List<Lab>> listLabs() {
        return ApiResponse.success(labService.listLabs());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Lab> createLab(@Valid @RequestBody LabRequest request) {
        return ApiResponse.success(labService.createLab(request));
    }
}

package org.example.lab_equipment_repair.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.ApiResponse;
import org.example.lab_equipment_repair.dto.SysDictItemRequest;
import org.example.lab_equipment_repair.dto.SysDictRequest;
import org.example.lab_equipment_repair.entity.SysDict;
import org.example.lab_equipment_repair.entity.SysDictItem;
import org.example.lab_equipment_repair.service.SysDictService;
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
@RequestMapping("/api/dicts")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService sysDictService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<SysDict>> listDicts(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(sysDictService.listDicts(keyword));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SysDict> createDict(@Valid @RequestBody SysDictRequest request) {
        return ApiResponse.success(sysDictService.createDict(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SysDict> updateDict(@PathVariable Long id, @Valid @RequestBody SysDictRequest request) {
        return ApiResponse.success(sysDictService.updateDict(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteDict(@PathVariable Long id) {
        sysDictService.deleteDict(id);
        return ApiResponse.success();
    }

    @GetMapping("/{dictCode}/items")
    public ApiResponse<List<SysDictItem>> listItems(
            @PathVariable String dictCode,
            @RequestParam(defaultValue = "true") Boolean enabledOnly) {
        return ApiResponse.success(sysDictService.listItems(dictCode, enabledOnly));
    }

    @PostMapping("/{dictId}/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SysDictItem> createItem(@PathVariable Long dictId, @Valid @RequestBody SysDictItemRequest request) {
        return ApiResponse.success(sysDictService.createItem(dictId, request));
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SysDictItem> updateItem(@PathVariable Long itemId, @Valid @RequestBody SysDictItemRequest request) {
        return ApiResponse.success(sysDictService.updateItem(itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteItem(@PathVariable Long itemId) {
        sysDictService.deleteItem(itemId);
        return ApiResponse.success();
    }
}

package org.example.lab_equipment_repair.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.ApiResponse;
import org.example.lab_equipment_repair.dto.AcceptanceRequest;
import org.example.lab_equipment_repair.dto.AssignOrderRequest;
import org.example.lab_equipment_repair.dto.FinishRepairRequest;
import org.example.lab_equipment_repair.dto.RemarkRequest;
import org.example.lab_equipment_repair.dto.RepairOrderQuery;
import org.example.lab_equipment_repair.dto.SubmitOrderRequest;
import org.example.lab_equipment_repair.dto.TransferOrderRequest;
import org.example.lab_equipment_repair.entity.OrderStatusLog;
import org.example.lab_equipment_repair.entity.RepairOrder;
import org.example.lab_equipment_repair.service.RepairOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/repair-orders")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    @GetMapping
    public ApiResponse<List<RepairOrder>> listOrders(RepairOrderQuery query) {
        return ApiResponse.success(repairOrderService.listOrders(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<RepairOrder> getOrder(@PathVariable Long id) {
        return ApiResponse.success(repairOrderService.getOrder(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('REPORTER') or hasRole('ADMIN')")
    public ApiResponse<RepairOrder> submitOrder(@Valid @RequestBody SubmitOrderRequest request) {
        return ApiResponse.success(repairOrderService.submitOrder(request));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('LAB_MANAGER') or hasRole('ADMIN')")
    public ApiResponse<RepairOrder> rejectOrder(@PathVariable Long id, @RequestBody RemarkRequest request) {
        return ApiResponse.success(repairOrderService.rejectOrder(id, request));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('LAB_MANAGER') or hasRole('ADMIN')")
    public ApiResponse<RepairOrder> assignOrder(@PathVariable Long id, @Valid @RequestBody AssignOrderRequest request) {
        return ApiResponse.success(repairOrderService.assignOrder(id, request));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasRole('REPAIRER') or hasRole('ADMIN')")
    public ApiResponse<RepairOrder> startRepair(@PathVariable Long id) {
        return ApiResponse.success(repairOrderService.startRepair(id));
    }

    @PutMapping("/{id}/finish")
    @PreAuthorize("hasRole('REPAIRER') or hasRole('ADMIN')")
    public ApiResponse<RepairOrder> finishRepair(@PathVariable Long id, @Valid @RequestBody FinishRepairRequest request) {
        return ApiResponse.success(repairOrderService.finishRepair(id, request));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('REPORTER') or hasRole('LAB_MANAGER') or hasRole('ADMIN')")
    public ApiResponse<RepairOrder> acceptOrder(@PathVariable Long id, @RequestBody AcceptanceRequest request) {
        return ApiResponse.success(repairOrderService.acceptOrder(id, request));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('REPORTER') or hasRole('LAB_MANAGER') or hasRole('ADMIN')")
    public ApiResponse<RepairOrder> cancelOrder(@PathVariable Long id, @RequestBody(required = false) RemarkRequest request) {
        return ApiResponse.success(repairOrderService.cancelOrder(id, request));
    }

    @PutMapping("/{id}/transfer")
    @PreAuthorize("hasRole('LAB_MANAGER') or hasRole('ADMIN')")
    public ApiResponse<RepairOrder> transferOrder(@PathVariable Long id, @Valid @RequestBody TransferOrderRequest request) {
        return ApiResponse.success(repairOrderService.transferOrder(id, request));
    }

    @PostMapping("/{id}/remarks")
    public ApiResponse<RepairOrder> addRemark(@PathVariable Long id, @RequestBody(required = false) RemarkRequest request) {
        return ApiResponse.success(repairOrderService.addRemark(id, request));
    }

    @GetMapping("/{id}/logs")
    public ApiResponse<List<OrderStatusLog>> listLogs(@PathVariable Long id) {
        return ApiResponse.success(repairOrderService.listLogs(id));
    }
}

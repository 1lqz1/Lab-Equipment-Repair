package org.example.lab_equipment_repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.BusinessException;
import org.example.lab_equipment_repair.dto.AcceptanceRequest;
import org.example.lab_equipment_repair.dto.AssignOrderRequest;
import org.example.lab_equipment_repair.dto.FinishRepairRequest;
import org.example.lab_equipment_repair.dto.RemarkRequest;
import org.example.lab_equipment_repair.dto.RepairOrderQuery;
import org.example.lab_equipment_repair.dto.SubmitOrderRequest;
import org.example.lab_equipment_repair.entity.AcceptanceRecord;
import org.example.lab_equipment_repair.entity.Equipment;
import org.example.lab_equipment_repair.entity.MaintenanceRecord;
import org.example.lab_equipment_repair.entity.OrderStatusLog;
import org.example.lab_equipment_repair.entity.RepairOrder;
import org.example.lab_equipment_repair.enums.EquipmentStatus;
import org.example.lab_equipment_repair.enums.OrderStatus;
import org.example.lab_equipment_repair.enums.UrgencyLevel;
import org.example.lab_equipment_repair.mapper.AcceptanceRecordMapper;
import org.example.lab_equipment_repair.mapper.EquipmentMapper;
import org.example.lab_equipment_repair.mapper.MaintenanceRecordMapper;
import org.example.lab_equipment_repair.mapper.OrderStatusLogMapper;
import org.example.lab_equipment_repair.mapper.RepairOrderMapper;
import org.example.lab_equipment_repair.security.LoginUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RepairOrderService {

    private final RepairOrderMapper repairOrderMapper;
    private final EquipmentMapper equipmentMapper;
    private final MaintenanceRecordMapper maintenanceRecordMapper;
    private final AcceptanceRecordMapper acceptanceRecordMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;

    public List<RepairOrder> listOrders(RepairOrderQuery query) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
                .eq(query.getStatus() != null && !query.getStatus().isBlank(), RepairOrder::getStatus, query.getStatus())
                .eq(query.getEquipmentId() != null, RepairOrder::getEquipmentId, query.getEquipmentId())
                .eq(query.getLabId() != null, RepairOrder::getLabId, query.getLabId())
                .eq(query.getReporterId() != null, RepairOrder::getReporterId, query.getReporterId())
                .eq(query.getAssignedTo() != null, RepairOrder::getAssignedTo, query.getAssignedTo())
                .orderByDesc(RepairOrder::getCreatedAt);
        return repairOrderMapper.selectList(wrapper);
    }

    public RepairOrder getOrder(Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("工单不存在");
        }
        return order;
    }

    @Transactional
    public RepairOrder submitOrder(SubmitOrderRequest request) {
        LoginUser loginUser = currentUser();
        Equipment equipment = equipmentMapper.selectById(request.getEquipmentId());
        if (equipment == null) {
            throw new BusinessException("设备不存在");
        }

        RepairOrder order = new RepairOrder();
        order.setOrderNo(generateOrderNo());
        order.setEquipmentId(equipment.getId());
        order.setLabId(equipment.getLabId());
        order.setReporterId(loginUser.getId());
        order.setFaultDescription(request.getFaultDescription());
        order.setUrgency(request.getUrgency() == null || request.getUrgency().isBlank()
                ? UrgencyLevel.NORMAL.name()
                : request.getUrgency());
        order.setLocation(request.getLocation());
        order.setContact(request.getContact());
        order.setStatus(OrderStatus.SUBMITTED.name());
        repairOrderMapper.insert(order);
        writeLog(order.getId(), null, OrderStatus.SUBMITTED.name(), "提交报修", "报修人提交工单");
        return order;
    }

    @Transactional
    public RepairOrder rejectOrder(Long id, RemarkRequest request) {
        RepairOrder order = getOrder(id);
        requireStatus(order, OrderStatus.SUBMITTED);
        changeStatus(order, OrderStatus.REJECTED, "退回报修", request.getRemark());
        return order;
    }

    @Transactional
    public RepairOrder assignOrder(Long id, AssignOrderRequest request) {
        RepairOrder order = getOrder(id);
        requireStatus(order, OrderStatus.SUBMITTED);
        order.setAssignedTo(request.getRepairerId());
        changeStatus(order, OrderStatus.ASSIGNED, "审核派单", request.getRemark());
        equipmentMapper.updateById(statusEquipment(order.getEquipmentId(), EquipmentStatus.REPAIRING));
        return order;
    }

    @Transactional
    public RepairOrder startRepair(Long id) {
        RepairOrder order = getOrder(id);
        requireStatus(order, OrderStatus.ASSIGNED);
        ensureAssignedRepairer(order);
        changeStatus(order, OrderStatus.IN_PROGRESS, "开始维修", "维修人员开始处理");
        return order;
    }

    @Transactional
    public RepairOrder finishRepair(Long id, FinishRepairRequest request) {
        RepairOrder order = getOrder(id);
        requireStatus(order, OrderStatus.IN_PROGRESS);
        ensureAssignedRepairer(order);

        MaintenanceRecord record = new MaintenanceRecord();
        record.setOrderId(order.getId());
        record.setRepairerId(currentUser().getId());
        record.setFaultReason(request.getFaultReason());
        record.setSolution(request.getSolution());
        record.setResult(request.getResult());
        record.setCost(request.getCost() == null ? BigDecimal.ZERO : request.getCost());
        record.setStartedAt(order.getUpdatedAt());
        record.setFinishedAt(LocalDateTime.now());
        maintenanceRecordMapper.insert(record);

        changeStatus(order, OrderStatus.REPAIRED, "完成维修", request.getResult());
        return order;
    }

    @Transactional
    public RepairOrder acceptOrder(Long id, AcceptanceRequest request) {
        RepairOrder order = getOrder(id);
        requireStatus(order, OrderStatus.REPAIRED);

        boolean accepted = request.getAccepted() == null || request.getAccepted();
        AcceptanceRecord record = new AcceptanceRecord();
        record.setOrderId(order.getId());
        record.setAccepterId(currentUser().getId());
        record.setAccepted(accepted);
        record.setComment(request.getComment());
        record.setAcceptedAt(LocalDateTime.now());
        acceptanceRecordMapper.insert(record);

        if (accepted) {
            changeStatus(order, OrderStatus.ACCEPTED, "验收通过", request.getComment());
            changeStatus(order, OrderStatus.CLOSED, "工单归档", "验收通过后自动归档");
            equipmentMapper.updateById(statusEquipment(order.getEquipmentId(), EquipmentStatus.NORMAL));
        } else {
            changeStatus(order, OrderStatus.IN_PROGRESS, "验收不通过", request.getComment());
            equipmentMapper.updateById(statusEquipment(order.getEquipmentId(), EquipmentStatus.REPAIRING));
        }
        return order;
    }

    public List<OrderStatusLog> listLogs(Long orderId) {
        return orderStatusLogMapper.selectList(new LambdaQueryWrapper<OrderStatusLog>()
                .eq(OrderStatusLog::getOrderId, orderId)
                .orderByAsc(OrderStatusLog::getCreatedAt));
    }

    private void changeStatus(RepairOrder order, OrderStatus toStatus, String operation, String remark) {
        String fromStatus = order.getStatus();
        order.setStatus(toStatus.name());
        repairOrderMapper.updateById(order);
        writeLog(order.getId(), fromStatus, toStatus.name(), operation, remark);
    }

    private void writeLog(Long orderId, String fromStatus, String toStatus, String operation, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(currentUser().getId());
        log.setOperation(operation);
        log.setRemark(remark);
        orderStatusLogMapper.insert(log);
    }

    private void requireStatus(RepairOrder order, OrderStatus status) {
        if (!status.name().equals(order.getStatus())) {
            throw new BusinessException("当前工单状态不允许执行该操作");
        }
    }

    private void ensureAssignedRepairer(RepairOrder order) {
        LoginUser loginUser = currentUser();
        if (!Objects.equals(order.getAssignedTo(), loginUser.getId()) && !"ADMIN".equals(loginUser.getRole())) {
            throw new BusinessException(403, "只能处理分配给自己的维修任务");
        }
    }

    private Equipment statusEquipment(Long equipmentId, EquipmentStatus status) {
        Equipment equipment = new Equipment();
        equipment.setId(equipmentId);
        equipment.setStatus(status.name());
        return equipment;
    }

    private LoginUser currentUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String generateOrderNo() {
        return "RO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}

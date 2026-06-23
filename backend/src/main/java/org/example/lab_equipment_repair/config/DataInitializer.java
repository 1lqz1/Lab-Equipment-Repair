package org.example.lab_equipment_repair.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.entity.Equipment;
import org.example.lab_equipment_repair.entity.Lab;
import org.example.lab_equipment_repair.entity.SysDict;
import org.example.lab_equipment_repair.entity.SysDictItem;
import org.example.lab_equipment_repair.entity.User;
import org.example.lab_equipment_repair.enums.EquipmentStatus;
import org.example.lab_equipment_repair.enums.OrderStatus;
import org.example.lab_equipment_repair.enums.UrgencyLevel;
import org.example.lab_equipment_repair.enums.UserRole;
import org.example.lab_equipment_repair.enums.UserStatus;
import org.example.lab_equipment_repair.mapper.EquipmentMapper;
import org.example.lab_equipment_repair.mapper.LabMapper;
import org.example.lab_equipment_repair.mapper.SysDictItemMapper;
import org.example.lab_equipment_repair.mapper.SysDictMapper;
import org.example.lab_equipment_repair.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserMapper userMapper;

    private final LabMapper labMapper;

    private final EquipmentMapper equipmentMapper;

    private final SysDictMapper sysDictMapper;

    private final SysDictItemMapper sysDictItemMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfAbsent("admin", "系统管理员", UserRole.ADMIN);
        createUserIfAbsent("manager", "实验室管理员", UserRole.LAB_MANAGER);
        createUserIfAbsent("repairer", "维修人员", UserRole.REPAIRER);
        createUserIfAbsent("reporter", "报修人员", UserRole.REPORTER);
        ensureActiveAdminExists();
        Long labId = createLabIfAbsent();
        createEquipmentIfAbsent("EQ-DEMO-001", "电子显微镜", "显微分析", labId, "A2-301", "2025-03-12");
        createEquipmentIfAbsent("EQ-DEMO-002", "高速离心机", "样品制备", labId, "A2-302", "2024-11-08");
        createEquipmentIfAbsent("EQ-DEMO-003", "恒温培养箱", "生物实验", labId, "A2-305", "2025-01-20");
        tryCreateDefaultDicts();
    }

    private void createUserIfAbsent(String username, String realName, UserRole role) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count > 0) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("123456"));
        user.setRealName(realName);
        user.setRole(role.name());
        user.setStatus(UserStatus.ACTIVE.name());
        userMapper.insert(user);
    }

    private void ensureActiveAdminExists() {
        Long activeAdminCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, UserRole.ADMIN.name())
                .eq(User::getStatus, UserStatus.ACTIVE.name()));
        if (activeAdminCount > 0) {
            return;
        }
        User admin = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
        if (admin == null) {
            return;
        }
        admin.setStatus(UserStatus.ACTIVE.name());
        userMapper.updateById(admin);
    }

    private Long createLabIfAbsent() {
        Lab lab = labMapper.selectOne(new LambdaQueryWrapper<Lab>().eq(Lab::getName, "现代仪器分析实验室"));
        if (lab != null) {
            return lab.getId();
        }
        User manager = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "manager"));
        lab = new Lab();
        lab.setName("现代仪器分析实验室");
        lab.setLocation("实验楼A座3层");
        lab.setManagerId(manager == null ? null : manager.getId());
        lab.setDescription("用于演示设备建档、报修提交和维修流转的默认实验室");
        labMapper.insert(lab);
        return lab.getId();
    }

    private void createEquipmentIfAbsent(
            String code,
            String name,
            String category,
            Long labId,
            String location,
            String purchaseDate) {
        Long count = equipmentMapper.selectCount(new LambdaQueryWrapper<Equipment>().eq(Equipment::getCode, code));
        if (count > 0) {
            return;
        }
        User manager = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, "manager"));
        Equipment equipment = new Equipment();
        equipment.setCode(code);
        equipment.setName(name);
        equipment.setCategory(category + " | " + location);
        equipment.setLabId(labId);
        equipment.setStatus(EquipmentStatus.NORMAL.name());
        equipment.setResponsibleUserId(manager == null ? null : manager.getId());
        equipment.setPurchaseDate(LocalDate.parse(purchaseDate));
        equipmentMapper.insert(equipment);
    }

    private void tryCreateDefaultDicts() {
        try {
            createDefaultDicts();
        } catch (RuntimeException exception) {
            log.warn("默认字典初始化跳过，请确认已执行 docs/horizontal_expansion_schema.sql：message={}", exception.getMessage());
        }
    }

    private void createDefaultDicts() {
        Long roleDictId = createDictIfAbsent("user_role", "用户角色", 10);
        createItemIfAbsent(roleDictId, UserRole.ADMIN.name(), "系统管理员", 10);
        createItemIfAbsent(roleDictId, UserRole.LAB_MANAGER.name(), "实验室管理员", 20);
        createItemIfAbsent(roleDictId, UserRole.REPAIRER.name(), "维修人员", 30);
        createItemIfAbsent(roleDictId, UserRole.REPORTER.name(), "报修人员", 40);

        Long userStatusDictId = createDictIfAbsent("user_status", "用户状态", 20);
        createItemIfAbsent(userStatusDictId, UserStatus.PENDING.name(), "待审核", 10);
        createItemIfAbsent(userStatusDictId, UserStatus.ACTIVE.name(), "启用", 20);
        createItemIfAbsent(userStatusDictId, UserStatus.REJECTED.name(), "已拒绝", 30);
        createItemIfAbsent(userStatusDictId, UserStatus.DISABLED.name(), "禁用", 40);

        Long equipmentStatusDictId = createDictIfAbsent("equipment_status", "设备状态", 30);
        createItemIfAbsent(equipmentStatusDictId, EquipmentStatus.NORMAL.name(), "正常", 10);
        createItemIfAbsent(equipmentStatusDictId, EquipmentStatus.REPAIRING.name(), "维修中", 20);
        createItemIfAbsent(equipmentStatusDictId, EquipmentStatus.DISABLED.name(), "停用", 30);
        createItemIfAbsent(equipmentStatusDictId, EquipmentStatus.SCRAPPED.name(), "已报废", 40);

        Long orderStatusDictId = createDictIfAbsent("order_status", "工单状态", 40);
        createItemIfAbsent(orderStatusDictId, OrderStatus.SUBMITTED.name(), "待审核", 10);
        createItemIfAbsent(orderStatusDictId, OrderStatus.REJECTED.name(), "已退回", 20);
        createItemIfAbsent(orderStatusDictId, OrderStatus.ASSIGNED.name(), "已派单", 30);
        createItemIfAbsent(orderStatusDictId, OrderStatus.IN_PROGRESS.name(), "维修中", 40);
        createItemIfAbsent(orderStatusDictId, OrderStatus.REPAIRED.name(), "待验收", 50);
        createItemIfAbsent(orderStatusDictId, OrderStatus.ACCEPTED.name(), "已验收", 60);
        createItemIfAbsent(orderStatusDictId, OrderStatus.CLOSED.name(), "已归档", 70);
        createItemIfAbsent(orderStatusDictId, OrderStatus.CANCELLED.name(), "已取消", 80);

        Long urgencyDictId = createDictIfAbsent("urgency_level", "紧急程度", 50);
        createItemIfAbsent(urgencyDictId, UrgencyLevel.LOW.name(), "低", 10);
        createItemIfAbsent(urgencyDictId, UrgencyLevel.NORMAL.name(), "普通", 20);
        createItemIfAbsent(urgencyDictId, UrgencyLevel.HIGH.name(), "高", 30);
        createItemIfAbsent(urgencyDictId, UrgencyLevel.URGENT.name(), "紧急", 40);

        Long categoryDictId = createDictIfAbsent("equipment_category", "设备分类", 60);
        createItemIfAbsent(categoryDictId, "显微分析", "显微分析", 10);
        createItemIfAbsent(categoryDictId, "样品制备", "样品制备", 20);
        createItemIfAbsent(categoryDictId, "生物实验", "生物实验", 30);
        createItemIfAbsent(categoryDictId, "电子测试", "电子测试", 40);
        createItemIfAbsent(categoryDictId, "教学设备", "教学设备", 50);
    }

    private Long createDictIfAbsent(String dictCode, String dictName, Integer sortOrder) {
        SysDict dict = sysDictMapper.selectOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode, dictCode));
        if (dict != null) {
            return dict.getId();
        }
        dict = new SysDict();
        dict.setDictCode(dictCode);
        dict.setDictName(dictName);
        dict.setEnabled(true);
        dict.setSortOrder(sortOrder);
        sysDictMapper.insert(dict);
        return dict.getId();
    }

    private void createItemIfAbsent(Long dictId, String itemValue, String itemLabel, Integer sortOrder) {
        Long count = sysDictItemMapper.selectCount(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictId, dictId)
                .eq(SysDictItem::getItemValue, itemValue));
        if (count > 0) {
            return;
        }
        SysDictItem item = new SysDictItem();
        item.setDictId(dictId);
        item.setItemValue(itemValue);
        item.setItemLabel(itemLabel);
        item.setEnabled(true);
        item.setSortOrder(sortOrder);
        sysDictItemMapper.insert(item);
    }
}

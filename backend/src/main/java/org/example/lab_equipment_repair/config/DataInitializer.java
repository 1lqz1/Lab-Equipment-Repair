package org.example.lab_equipment_repair.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.entity.Equipment;
import org.example.lab_equipment_repair.entity.Lab;
import org.example.lab_equipment_repair.entity.User;
import org.example.lab_equipment_repair.enums.EquipmentStatus;
import org.example.lab_equipment_repair.enums.UserRole;
import org.example.lab_equipment_repair.enums.UserStatus;
import org.example.lab_equipment_repair.mapper.EquipmentMapper;
import org.example.lab_equipment_repair.mapper.LabMapper;
import org.example.lab_equipment_repair.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;

    private final LabMapper labMapper;

    private final EquipmentMapper equipmentMapper;

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
}

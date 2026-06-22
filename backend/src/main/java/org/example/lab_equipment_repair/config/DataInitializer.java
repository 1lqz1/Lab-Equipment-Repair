package org.example.lab_equipment_repair.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.entity.User;
import org.example.lab_equipment_repair.enums.UserRole;
import org.example.lab_equipment_repair.enums.UserStatus;
import org.example.lab_equipment_repair.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfAbsent("admin", "系统管理员", UserRole.ADMIN);
        createUserIfAbsent("manager", "实验室管理员", UserRole.LAB_MANAGER);
        createUserIfAbsent("repairer", "维修人员", UserRole.REPAIRER);
        createUserIfAbsent("reporter", "报修人", UserRole.REPORTER);
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
}

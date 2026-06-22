package org.example.lab_equipment_repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.BusinessException;
import org.example.lab_equipment_repair.dto.CreateUserRequest;
import org.example.lab_equipment_repair.dto.RegisterRequest;
import org.example.lab_equipment_repair.dto.UserResponse;
import org.example.lab_equipment_repair.entity.User;
import org.example.lab_equipment_repair.enums.UserRole;
import org.example.lab_equipment_repair.enums.UserStatus;
import org.example.lab_equipment_repair.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> listUsers(String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(role != null && !role.isBlank(), User::getRole, role)
                .orderByDesc(User::getCreatedAt);
        return userMapper.selectList(wrapper).stream().map(UserResponse::from).toList();
    }

    public UserResponse register(RegisterRequest request) {
        log.info("注册申请：username={}, realName={}, phone={}, role={}, passwordProvided={}, passwordLength={}",
                request.username(),
                request.realName(),
                request.phone(),
                UserRole.REPORTER.name(),
                request.password() != null && !request.password().isBlank(),
                request.password() == null ? 0 : request.password().length());
        ensureUsernameAvailable(request.username());
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRealName(request.realName());
        user.setPhone(request.phone());
        user.setRole(UserRole.REPORTER.name());
        user.setStatus(UserStatus.PENDING.name());
        userMapper.insert(user);
        log.info("注册申请已保存：username={}, userId={}, role={}, status={}",
                user.getUsername(), user.getId(), user.getRole(), user.getStatus());
        return UserResponse.from(user);
    }

    public UserResponse createUser(CreateUserRequest request) {
        log.info("管理员创建用户：username={}, realName={}, phone={}, role={}, passwordProvided={}, passwordLength={}",
                request.username(),
                request.realName(),
                request.phone(),
                request.role(),
                request.password() != null && !request.password().isBlank(),
                request.password() == null ? 0 : request.password().length());
        ensureUsernameAvailable(request.username());
        UserRole role = parseRole(request.role());
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRealName(request.realName());
        user.setPhone(request.phone());
        user.setRole(role.name());
        user.setStatus(UserStatus.ACTIVE.name());
        userMapper.insert(user);
        log.info("管理员创建用户成功：username={}, userId={}, role={}, status={}",
                user.getUsername(), user.getId(), user.getRole(), user.getStatus());
        return UserResponse.from(user);
    }

    public UserResponse approve(Long id) {
        return updateStatus(id, UserStatus.ACTIVE);
    }

    public UserResponse reject(Long id) {
        return updateStatus(id, UserStatus.REJECTED);
    }

    public UserResponse disable(Long id) {
        return updateStatus(id, UserStatus.DISABLED);
    }

    public UserResponse enable(Long id) {
        return updateStatus(id, UserStatus.ACTIVE);
    }

    private UserResponse updateStatus(Long id, UserStatus status) {
        User user = getById(id);
        String oldStatus = user.getStatus();
        user.setStatus(status.name());
        userMapper.updateById(user);
        log.info("用户状态变更：userId={}, username={}, oldStatus={}, newStatus={}",
                user.getId(), user.getUsername(), oldStatus, user.getStatus());
        return UserResponse.from(user);
    }

    private User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private void ensureUsernameAvailable(String username) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count > 0) {
            log.warn("账号占用：username={}", username);
            throw new BusinessException("账号已存在");
        }
    }

    private UserRole parseRole(String role) {
        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            log.warn("用户角色不合法：role={}", role);
            throw new BusinessException("用户角色不合法");
        }
    }
}

package org.example.lab_equipment_repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.BusinessException;
import org.example.lab_equipment_repair.dto.CreateUserRequest;
import org.example.lab_equipment_repair.dto.RegisterRequest;
import org.example.lab_equipment_repair.dto.UpdateUserRequest;
import org.example.lab_equipment_repair.dto.UserResponse;
import org.example.lab_equipment_repair.entity.User;
import org.example.lab_equipment_repair.enums.UserRole;
import org.example.lab_equipment_repair.enums.UserStatus;
import org.example.lab_equipment_repair.mapper.UserMapper;
import org.example.lab_equipment_repair.security.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> listUsers(String role, String status, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(role != null && !role.isBlank(), User::getRole, role)
                .eq(status != null && !status.isBlank(), User::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), query -> query
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getRealName, keyword)
                        .or()
                        .like(User::getPhone, keyword))
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
        ensureCanDisable(id);
        return updateStatus(id, UserStatus.DISABLED);
    }

    public UserResponse enable(Long id) {
        return updateStatus(id, UserStatus.ACTIVE);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = getById(id);
        UserRole role = parseRole(request.role());
        UserStatus status = parseStatus(request.status());
        if (UserStatus.DISABLED.equals(status)) {
            ensureCanDisable(id);
        }
        user.setRealName(request.realName().trim());
        user.setPhone(normalize(request.phone()));
        user.setRole(role.name());
        user.setStatus(status.name());
        userMapper.updateById(user);
        log.info("管理员更新用户：userId={}, username={}, role={}, status={}",
                user.getId(), user.getUsername(), user.getRole(), user.getStatus());
        return UserResponse.from(user);
    }

    public void resetPassword(Long id, String password) {
        User user = getById(id);
        user.setPasswordHash(passwordEncoder.encode(password));
        userMapper.updateById(user);
        log.info("管理员重置用户密码：userId={}, username={}, passwordLength={}",
                user.getId(), user.getUsername(), password == null ? 0 : password.length());
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

    private void ensureCanDisable(Long id) {
        LoginUser currentUser = currentLoginUser();
        if (currentUser != null && Objects.equals(currentUser.getId(), id)) {
            log.warn("阻止禁用当前登录账号：userId={}, username={}", currentUser.getId(), currentUser.getUsername());
            throw new BusinessException("不能禁用当前登录账号");
        }
        User targetUser = getById(id);
        if (!UserRole.ADMIN.name().equals(targetUser.getRole())) {
            return;
        }
        Long activeAdminCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, UserRole.ADMIN.name())
                .eq(User::getStatus, UserStatus.ACTIVE.name()));
        if (activeAdminCount <= 1 && UserStatus.ACTIVE.name().equals(targetUser.getStatus())) {
            log.warn("阻止禁用最后一个启用的管理员：userId={}, username={}", targetUser.getId(), targetUser.getUsername());
            throw new BusinessException("不能禁用最后一个启用的管理员");
        }
    }

    private LoginUser currentLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser;
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

    private UserStatus parseStatus(String status) {
        try {
            return UserStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            log.warn("用户状态不合法：status={}", status);
            throw new BusinessException("用户状态不合法");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

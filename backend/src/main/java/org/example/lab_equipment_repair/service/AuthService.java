package org.example.lab_equipment_repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.BusinessException;
import org.example.lab_equipment_repair.dto.LoginRequest;
import org.example.lab_equipment_repair.dto.LoginResponse;
import org.example.lab_equipment_repair.dto.UserResponse;
import org.example.lab_equipment_repair.entity.User;
import org.example.lab_equipment_repair.enums.UserStatus;
import org.example.lab_equipment_repair.mapper.UserMapper;
import org.example.lab_equipment_repair.security.JwtTokenProvider;
import org.example.lab_equipment_repair.security.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserMapper userMapper;

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        log.info("登录尝试：username={}, passwordProvided={}, passwordLength={}",
                username,
                password != null && !password.isBlank(),
                password == null ? 0 : password.length());

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            log.warn("登录失败：username={}, reason=USER_NOT_FOUND", username);
            throw new BusinessException(401, "账号或密码错误");
        }
        validateLoginStatus(user);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException exception) {
            log.warn("登录失败：username={}, userId={}, role={}, reason=BAD_CREDENTIALS",
                    username, user.getId(), user.getRole());
            throw new BusinessException(401, "账号或密码错误");
        } catch (AuthenticationException exception) {
            log.warn("登录失败：username={}, userId={}, role={}, reason=AUTHENTICATION_EXCEPTION, exception={}",
                    username, user.getId(), user.getRole(), exception.getClass().getSimpleName());
            throw new BusinessException(401, "账号或密码错误");
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = jwtTokenProvider.createToken(loginUser);
        log.info("登录成功：username={}, userId={}, role={}, status={}",
                loginUser.getUsername(),
                loginUser.getId(),
                loginUser.getRole(),
                loginUser.getUser().getStatus());
        return new LoginResponse(token, UserResponse.from(loginUser.getUser()));
    }

    public UserResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return UserResponse.from(loginUser.getUser());
    }

    private void validateLoginStatus(User user) {
        String status = user.getStatus();
        if (UserStatus.ACTIVE.name().equals(status)) {
            return;
        }
        log.warn("登录拒绝：username={}, userId={}, role={}, status={}",
                user.getUsername(), user.getId(), user.getRole(), status);
        if (UserStatus.PENDING.name().equals(status)) {
            throw new BusinessException(403, "账号待管理员审核");
        }
        if (UserStatus.REJECTED.name().equals(status)) {
            throw new BusinessException(403, "注册申请已被拒绝");
        }
        if (UserStatus.DISABLED.name().equals(status)) {
            throw new BusinessException(403, "账号已被禁用");
        }
        throw new BusinessException(403, "账号状态异常");
    }
}

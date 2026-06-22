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

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserMapper userMapper;

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "账号或密码错误");
        }
        validateLoginStatus(user.getStatus());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException exception) {
            throw new BusinessException(401, "账号或密码错误");
        } catch (AuthenticationException exception) {
            throw new BusinessException(401, "账号或密码错误");
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = jwtTokenProvider.createToken(loginUser);
        return new LoginResponse(token, UserResponse.from(loginUser.getUser()));
    }

    public UserResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return UserResponse.from(loginUser.getUser());
    }

    private void validateLoginStatus(String status) {
        if (UserStatus.ACTIVE.name().equals(status)) {
            return;
        }
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

package org.example.lab_equipment_repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.dto.UserResponse;
import org.example.lab_equipment_repair.entity.User;
import org.example.lab_equipment_repair.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public List<UserResponse> listUsers(String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(role != null && !role.isBlank(), User::getRole, role)
                .orderByDesc(User::getCreatedAt);
        return userMapper.selectList(wrapper).stream().map(UserResponse::from).toList();
    }
}

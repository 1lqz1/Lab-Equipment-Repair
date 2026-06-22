package org.example.lab_equipment_repair.service;

import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.BusinessException;
import org.example.lab_equipment_repair.dto.UpdateProfileRequest;
import org.example.lab_equipment_repair.dto.UserResponse;
import org.example.lab_equipment_repair.entity.User;
import org.example.lab_equipment_repair.mapper.UserMapper;
import org.example.lab_equipment_repair.security.LoginUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final UserMapper userMapper;

    @Value("${app.upload.avatar-dir:uploads/avatars}")
    private String avatarDir;

    public UserResponse currentProfile() {
        return UserResponse.from(currentUser());
    }

    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = currentUser();
        user.setRealName(request.realName());
        user.setPhone(request.phone());
        userMapper.updateById(user);
        return UserResponse.from(user);
    }

    public UserResponse uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择头像文件");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException("头像仅支持 JPG、PNG、WEBP 格式");
        }
        String extension = extensionOf(file.getContentType());
        String filename = UUID.randomUUID() + "." + extension.toLowerCase();
        Path uploadDir = Path.of(avatarDir).toAbsolutePath().normalize();
        Path target = uploadDir.resolve(filename).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new BusinessException("头像文件路径不合法");
        }

        try {
            Files.createDirectories(uploadDir);
            file.transferTo(target);
        } catch (IOException exception) {
            throw new BusinessException("头像上传失败");
        }

        User user = currentUser();
        user.setAvatarPath("/uploads/avatars/" + filename);
        userMapper.updateById(user);
        return UserResponse.from(user);
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private String extensionOf(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}

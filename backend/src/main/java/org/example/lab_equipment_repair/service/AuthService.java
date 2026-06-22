package org.example.lab_equipment_repair.service;

import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.dto.LoginRequest;
import org.example.lab_equipment_repair.dto.LoginResponse;
import org.example.lab_equipment_repair.dto.UserResponse;
import org.example.lab_equipment_repair.security.JwtTokenProvider;
import org.example.lab_equipment_repair.security.LoginUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = jwtTokenProvider.createToken(loginUser);
        return new LoginResponse(token, UserResponse.from(loginUser.getUser()));
    }

    public UserResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return UserResponse.from(loginUser.getUser());
    }
}

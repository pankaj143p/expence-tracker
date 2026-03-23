package com.expense.service;

import com.expense.dto.AuthDTO;
import com.expense.model.User;
import com.expense.repository.UserRepository;
import com.expense.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername()))
            throw new RuntimeException("Username already taken");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already registered");

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthDTO.AuthResponse(token, user.getUsername(), user.getId());
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest req) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        User user = userRepository.findByUsername(req.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthDTO.AuthResponse(token, user.getUsername(), user.getId());
    }
}

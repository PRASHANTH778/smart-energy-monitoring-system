package com.smartenergy.service;

import com.smartenergy.config.JwtTokenProvider;
import com.smartenergy.dto.*;
import com.smartenergy.exception.BadRequestException;
import com.smartenergy.exception.ResourceNotFoundException;
import com.smartenergy.entity.Role;
import com.smartenergy.entity.User;
import com.smartenergy.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
                       PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse authenticateUser(AuthRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getRole().name());
    }

    @Transactional
    public UserDto registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new BadRequestException("Username '" + signUpRequest.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email '" + signUpRequest.getEmail() + "' is already registered");
        }

        Role role = Role.ROLE_OPERATOR;
        if (signUpRequest.getRole() != null) {
            try {
                role = Role.valueOf(signUpRequest.getRole());
            } catch (IllegalArgumentException e) {
                role = Role.ROLE_OPERATOR;
            }
        }

        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFullName(),
                role
        );

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    public UserDto getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToDto(user);
    }

    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public UserDto updateProfile(String fullName, String email) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (email != null && !email.equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new BadRequestException("Email already taken by another account");
            }
            user.setEmail(email);
        }

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }

        return mapToDto(userRepository.save(user));
    }

    private UserDto mapToDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getRole().name(), user.isActive(), user.getCreatedAt());
    }
}


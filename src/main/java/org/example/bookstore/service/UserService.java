package org.example.bookstore.service;

import org.example.bookstore.dto.user.UserRegistrationRequestDto;
import org.example.bookstore.dto.user.UserResponseDto;
import org.example.bookstore.dto.user.UserUpdateDto;
import org.example.bookstore.exception.RegistrationException;
import org.example.bookstore.model.Role;
import org.example.bookstore.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserResponseDto addRole(String email, Role.RoleName roleName);

    UserResponseDto getCurrentUserInfo(User user);

    UserResponseDto update(UserUpdateDto updateDto, User user);
}

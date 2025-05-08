package org.example.bookstore.service.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.bookstore.config.AdminProperties;
import org.example.bookstore.dto.user.UserRegistrationRequestDto;
import org.example.bookstore.dto.user.UserResponseDto;
import org.example.bookstore.dto.user.UserUpdateDto;
import org.example.bookstore.exception.EntityNotFoundException;
import org.example.bookstore.exception.RegistrationException;
import org.example.bookstore.mapper.UserMapper;
import org.example.bookstore.model.Role;
import org.example.bookstore.model.User;
import org.example.bookstore.repository.RoleRepository;
import org.example.bookstore.repository.UserRepository;
import org.example.bookstore.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AdminProperties adminProperties;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(
            UserRegistrationRequestDto requestDto) throws RegistrationException {
        checkForUserPresenceByEmail(requestDto.email());
        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        user.setRoles(getRolesByEmail(requestDto.email(), requestDto.inviteCode()));
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return getUserByEmail(email);
    }

    @Override
    public UserResponseDto addRole(String email, Role.RoleName roleName) {
        User user = getUserByEmail(email);
        Role role = getRoleByName(roleName);
        user.addRole(role);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto getCurrentUserInfo(User user) {
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto update(UserUpdateDto updateDto, User user) {
        User updatedUser = userMapper.toModel(updateDto);
        return userMapper.toDto(userRepository.save(updatedUser));
    }

    private Set<Role> getRolesByEmail(String email, String inviteCode)
            throws RegistrationException {
        Set<Role> roles = new HashSet<>();
        roles.add(getRoleByName(Role.RoleName.USER));
        if (Objects.equals(adminProperties.getEmail(), email)) {
            if (!Objects.equals(inviteCode, adminProperties.getInviteCode())) {
                throw new RegistrationException("Invalid inviteCode");
            }
            roles.add(getRoleByName(Role.RoleName.ADMIN));
        }
        return roles;
    }

    private User getUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
                String.format("A user with email %s does not exist", email)));
    }

    private Role getRoleByName(Role.RoleName roleName) {
        return roleRepository.findByName(roleName).orElseThrow(
                () -> new EntityNotFoundException("Can't find a role with name " + roleName));
    }

    private void checkForUserPresenceByEmail(String email) throws RegistrationException {
        if (userRepository.existsByEmail(email)) {
            throw new RegistrationException(
                    String.format("A user with email %s already exists", email));
        }
    }
}

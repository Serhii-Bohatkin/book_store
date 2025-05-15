package bookstore.service.impl;

import static bookstore.exception.EntityNotFoundException.entityNotFoundException;

import bookstore.config.AdminProperties;
import bookstore.dto.user.UserRegistrationRequestDto;
import bookstore.dto.user.UserResponseDto;
import bookstore.dto.user.UserUpdateDto;
import bookstore.exception.RegistrationException;
import bookstore.mapper.UserMapper;
import bookstore.model.Role;
import bookstore.model.User;
import bookstore.repository.RoleRepository;
import bookstore.repository.ShoppingCartRepository;
import bookstore.repository.UserRepository;
import bookstore.service.UserService;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    public static final String USER_NOT_FOUND_MESSAGE = "A user with email %s already exists";
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AdminProperties adminProperties;
    private final RoleRepository roleRepository;
    private final ShoppingCartRepository shoppingCartRepository;

    @Transactional
    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        throwExceptionIfUserExists(requestDto.email());
        User user = userMapper.toModel(requestDto)
                .setPassword(passwordEncoder.encode(requestDto.password()))
                .setRoles(assignRolesByEmail(requestDto.email(), requestDto.inviteCode()));
        User savedUser = userRepository.save(user);
        shoppingCartRepository.save(savedUser.createShoppingCart());
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
                String.format(USER_NOT_FOUND_MESSAGE, email)));
    }

    @Transactional
    @Override
    public UserResponseDto addRole(String email, Role.RoleName roleName) {
        User user = (User) loadUserByUsername(email);
        if (user.hasRole(roleName)) {
            throw new IllegalStateException(String.format(
                    "User with email %s already has the %s role", email, roleName));
        }
        Role role = getRoleByName(roleName);
        user.addRole(role);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto getCurrentUserInfo(User user) {
        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public UserResponseDto update(UserUpdateDto updateDto, User user) {
        userMapper.updateUser(user, updateDto);
        return userMapper.toDto(userRepository.save(user));
    }

    private Set<Role> assignRolesByEmail(String email, String inviteCode) {
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

    private Role getRoleByName(Role.RoleName roleName) {
        return roleRepository.findByName(roleName).orElseThrow(
                entityNotFoundException("Can't find a role with name {0}", roleName));
    }

    private void throwExceptionIfUserExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RegistrationException(USER_NOT_FOUND_MESSAGE, email);
        }
    }
}

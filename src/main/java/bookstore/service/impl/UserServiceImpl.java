package bookstore.service.impl;

import bookstore.config.AdminProperties;
import bookstore.dto.user.UserRegistrationRequestDto;
import bookstore.dto.user.UserResponseDto;
import bookstore.dto.user.UserUpdateDto;
import bookstore.exception.EntityNotFoundException;
import bookstore.exception.RegistrationException;
import bookstore.mapper.UserMapper;
import bookstore.model.Role;
import bookstore.model.ShoppingCart;
import bookstore.model.User;
import bookstore.repository.RoleRepository;
import bookstore.repository.ShoppingCartRepository;
import bookstore.repository.UserRepository;
import bookstore.service.UserService;
import java.text.MessageFormat;
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
    private static final String USER_ALREADY_EXISTS_MESSAGE =
            "A user with email {0} already exists";
    private static final String USER_NOT_FOUND_MESSAGE = "A user with email {0} does not exist";
    private static final String USER_ALREADY_HAS_ROLE_MESSAGE =
            "User with email %s already has the %s role";
    private static final String INCORRECT_INVITE_CODE_MESSAGE = "{0} is an incorrect invite code";
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
        shoppingCartRepository.save(new ShoppingCart(savedUser));
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
                        MessageFormat.format(USER_NOT_FOUND_MESSAGE, email)));
    }

    @Transactional
    @Override
    public UserResponseDto addRole(String email, Role.RoleName roleName) {
        User user = getUserByEmailOrThrow(email);
        if (user.hasRole(roleName)) {
            throw new IllegalStateException(
                    String.format(USER_ALREADY_HAS_ROLE_MESSAGE, email, roleName));
        }
        Role role = roleRepository.getByName(roleName);
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

    private User getUserByEmailOrThrow(String email) {
        try {
            return (User) loadUserByUsername(email);
        } catch (UsernameNotFoundException ex) {
            throw new EntityNotFoundException(USER_NOT_FOUND_MESSAGE, email);
        }
    }

    private Set<Role> assignRolesByEmail(String email, String inviteCode) {
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.getByName(Role.RoleName.USER));
        if (Objects.equals(adminProperties.getEmail(), email)) {
            if (!Objects.equals(inviteCode, adminProperties.getInviteCode())) {
                throw new RegistrationException(INCORRECT_INVITE_CODE_MESSAGE, inviteCode);
            }
            roles.add(roleRepository.getByName(Role.RoleName.ADMIN));
        }
        return roles;
    }

    private void throwExceptionIfUserExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RegistrationException(USER_ALREADY_EXISTS_MESSAGE, email);
        }
    }
}

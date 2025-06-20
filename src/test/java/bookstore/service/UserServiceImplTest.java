package bookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import bookstore.TestObjectsFactory;
import bookstore.config.AdminProperties;
import bookstore.dto.user.UserRegistrationRequestDto;
import bookstore.dto.user.UserResponseDto;
import bookstore.dto.user.UserUpdateDto;
import bookstore.exception.RegistrationException;
import bookstore.mapper.UserMapper;
import bookstore.model.Role;
import bookstore.model.ShoppingCart;
import bookstore.model.User;
import bookstore.repository.RoleRepository;
import bookstore.repository.ShoppingCartRepository;
import bookstore.repository.UserRepository;
import bookstore.service.impl.UserServiceImpl;
import java.text.MessageFormat;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private static final String INCORRECT_INVITE_CODE_MESSAGE = "{0} is an incorrect invite code";
    private static final String USER_ALREADY_EXISTS_MESSAGE =
            "A user with email {0} already exists";
    private static final String USER_NOT_FOUND_MESSAGE = "A user with email %s does not exist";
    private static final String USER_ALREADY_HAS_ROLE_MESSAGE =
            "User with email %s already has the %s role";
    private static final String HASHED_PASSWORD =
            "$2a$10$TiWXRP/UCiXdc21fpBvQu.HqHwVuvPPZH7/FgUsHMDnLq2sxlSD62";
    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String USER_EMAIL = "user@gmail.com";
    private static final String INVITE_CODE = "someInviteCode";
    private static final String WRONG_INVITE_CODE = "wrongCode";

    private User user;
    private UserResponseDto userResponseDto;
    private Role adminRole;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AdminProperties adminProperties;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        user = TestObjectsFactory.createUser();
        userResponseDto = TestObjectsFactory.createUserResponseDto(USER_EMAIL);
        adminRole = TestObjectsFactory.createAdminRole();
    }

    @AfterEach
    void verifyNoUnexpectedInteractions() {
        verifyNoMoreInteractions(passwordEncoder, userRepository, userMapper, adminProperties,
                roleRepository, shoppingCartRepository);
    }

    @Nested
    class RegisterMethodTests {
        private Role userRole;

        @BeforeEach
        void setUp() {
            userRole = TestObjectsFactory.createUserRole();
        }

        @Test
        @DisplayName("Should register a new user when user is not already registered")
        void register_UserNotRegisteredYet_ShouldRegisterUserWithRoleUser() {
            UserRegistrationRequestDto userRegistrationRequestDto =
                    TestObjectsFactory.createRegistrationRequestDto(USER_EMAIL, null);
            ShoppingCart shoppingCart = TestObjectsFactory.createEmptyShoppingCart();

            when(userRepository.existsByEmail(userRegistrationRequestDto.email())).thenReturn(
                    false);
            when(userMapper.toModel(userRegistrationRequestDto)).thenReturn(user);
            when(passwordEncoder.encode(userRegistrationRequestDto.password()))
                    .thenReturn(HASHED_PASSWORD);
            when(roleRepository.getByName(Role.RoleName.USER)).thenReturn(userRole);
            when(adminProperties.getEmail()).thenReturn(ADMIN_EMAIL);
            when(userRepository.save(user)).thenReturn(user);
            when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(
                    shoppingCart);
            when(userMapper.toDto(user)).thenReturn(userResponseDto);

            UserResponseDto actual = userService.register(userRegistrationRequestDto);

            assertThat(actual).isEqualTo(userResponseDto);
            verify(userRepository).existsByEmail(USER_EMAIL);
            verify(userMapper).toModel(userRegistrationRequestDto);
            verify(passwordEncoder).encode(userRegistrationRequestDto.password());
            verify(roleRepository).getByName(Role.RoleName.USER);
            verify(adminProperties).getEmail();
            verify(userRepository).save(user);
            verify(shoppingCartRepository).save(any(ShoppingCart.class));
            verify(userMapper).toDto(user);
        }

        @Test
        @DisplayName("Should throw RegistrationException when user is already registered")
        void register_UserAlreadyRegistered_ShouldThrowRegistrationException() {
            UserRegistrationRequestDto userRegistrationRequestDto =
                    TestObjectsFactory.createRegistrationRequestDto(USER_EMAIL, null);

            when(userRepository.existsByEmail(userRegistrationRequestDto.email())).thenReturn(true);

            RegistrationException ex = assertThrows(RegistrationException.class,
                    () -> userService.register(userRegistrationRequestDto));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(USER_ALREADY_EXISTS_MESSAGE,
                            userRegistrationRequestDto.email()));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should register a new ADMIN user when email matches and invite code is valid")
        void register_AdminEmailAndValidInviteCode_ShouldRegisterUserWithRoleAdmin() {
            UserRegistrationRequestDto adminRegistrationRequestDto =
                    TestObjectsFactory.createRegistrationRequestDto(ADMIN_EMAIL, INVITE_CODE);
            UserResponseDto userResponseDtoAdmin =
                    TestObjectsFactory.createUserResponseDto(ADMIN_EMAIL);
            ShoppingCart shoppingCart = TestObjectsFactory.createEmptyShoppingCart();

            when(userRepository.existsByEmail(adminRegistrationRequestDto.email())).thenReturn(
                    false);
            when(userMapper.toModel(adminRegistrationRequestDto)).thenReturn(user);
            when(passwordEncoder.encode(adminRegistrationRequestDto.password()))
                    .thenReturn(HASHED_PASSWORD);
            when(roleRepository.getByName(Role.RoleName.USER)).thenReturn(userRole);
            when(adminProperties.getEmail()).thenReturn(ADMIN_EMAIL);
            when(adminProperties.getInviteCode()).thenReturn(INVITE_CODE);
            when(roleRepository.getByName(Role.RoleName.ADMIN)).thenReturn(adminRole);
            when(userRepository.save(user)).thenReturn(user);
            when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(shoppingCart);
            when(userMapper.toDto(user)).thenReturn(userResponseDtoAdmin);

            UserResponseDto actual = userService.register(adminRegistrationRequestDto);

            assertThat(actual).isEqualTo(userResponseDtoAdmin);
            verify(userRepository).existsByEmail(adminRegistrationRequestDto.email());
            verify(userMapper).toModel(adminRegistrationRequestDto);
            verify(passwordEncoder).encode(adminRegistrationRequestDto.password());
            verify(roleRepository).getByName(Role.RoleName.USER);
            verify(adminProperties).getEmail();
            verify(adminProperties).getInviteCode();
            verify(roleRepository).getByName(Role.RoleName.ADMIN);
            verify(userRepository).save(user);
            verify(shoppingCartRepository).save(any(ShoppingCart.class));
            verify(userMapper).toDto(user);
        }

        @Test
        @DisplayName(
                "Should throw RegistrationException when email matches but invite code is invalid")
        void register_AdminEmailAndInvalidInviteCode_ShouldThrowRegistrationException() {
            UserRegistrationRequestDto adminRegistrationRequestDto =
                    TestObjectsFactory.createRegistrationRequestDto(ADMIN_EMAIL, WRONG_INVITE_CODE);

            when(userRepository.existsByEmail(adminRegistrationRequestDto.email())).thenReturn(
                    false);
            when(userMapper.toModel(adminRegistrationRequestDto)).thenReturn(user);
            when(passwordEncoder.encode(adminRegistrationRequestDto.password()))
                    .thenReturn(HASHED_PASSWORD);
            when(roleRepository.getByName(Role.RoleName.USER)).thenReturn(userRole);
            when(adminProperties.getEmail()).thenReturn(ADMIN_EMAIL);
            when(adminProperties.getInviteCode()).thenReturn(INVITE_CODE);

            RegistrationException ex = assertThrows(RegistrationException.class,
                    () -> userService.register(adminRegistrationRequestDto));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(INCORRECT_INVITE_CODE_MESSAGE, WRONG_INVITE_CODE));
            verify(userRepository, never()).save(any(User.class));
            verify(userRepository).existsByEmail(adminRegistrationRequestDto.email());
            verify(userMapper).toModel(adminRegistrationRequestDto);
            verify(passwordEncoder).encode(adminRegistrationRequestDto.password());
            verify(roleRepository).getByName(Role.RoleName.USER);
            verify(adminProperties).getEmail();
            verify(adminProperties).getInviteCode();
        }
    }

    @Nested
    class LoadUserByUsernameMethodTests {
        @Test
        @DisplayName("Should return UserDetails when user is found by email")
        void loadUserByUsername_UserFound_ShouldReturnUserDetails() {
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

            UserDetails actual = userService.loadUserByUsername(USER_EMAIL);

            assertThat(actual).isEqualTo(user);
            verify(userRepository).findByEmail(USER_EMAIL);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user is not found by email")
        void loadUserByUsername_UserNotFound_ShouldThrowUsernameNotFoundException() {
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                    () -> userService.loadUserByUsername(USER_EMAIL));

            assertThat(ex.getMessage()).isEqualTo(
                    String.format(USER_NOT_FOUND_MESSAGE, USER_EMAIL));
            verify(userRepository).findByEmail(USER_EMAIL);
        }
    }

    @Nested
    class AddRoleMethodTests {
        @Test
        @DisplayName("Should add a new role to the user when user does not have it yet")
        void addRole_UserDoesNotHaveRoleYet_ShouldAddNewRole() {
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(roleRepository.getByName(Role.RoleName.ADMIN)).thenReturn(adminRole);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toDto(user)).thenReturn(userResponseDto);

            UserResponseDto actual = userService.addRole(USER_EMAIL, Role.RoleName.ADMIN);

            assertThat(actual).isEqualTo(userResponseDto);
            verify(userRepository).findByEmail(USER_EMAIL);
            verify(roleRepository).getByName(Role.RoleName.ADMIN);
            verify(userRepository).save(user);
            verify(userMapper).toDto(user);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when user already has the role")
        void addRole_UserAlreadyHasRole_ShouldAddNewRole() {
            user.addRole(adminRole);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> userService.addRole(USER_EMAIL, Role.RoleName.ADMIN));

            assertThat(ex.getMessage()).isEqualTo(
                    String.format(USER_ALREADY_HAS_ROLE_MESSAGE, USER_EMAIL, Role.RoleName.ADMIN));
            verify(userRepository).findByEmail(USER_EMAIL);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    @DisplayName(
            "getCurrentUserInfo(): Should return UserResponseDto when getting current user info")
    void getCurrentUserInfo_ShouldReturnUserResponseDto() {
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto actual = userService.getCurrentUserInfo(user);

        assertThat(actual).isEqualTo(userResponseDto);
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("update(): Should return UserResponseDto when updating user information")
    void update_ShouldReturnUserResponseDto() {
        UserUpdateDto updateDto = TestObjectsFactory.createUserUpdateDto("John");

        doNothing().when(userMapper).updateUser(user, updateDto);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto actual = userService.update(updateDto, user);

        assertThat(actual).isEqualTo(userResponseDto);
        verify(userMapper).updateUser(user, updateDto);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }
}

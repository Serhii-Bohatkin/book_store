package bookstore.service;

import bookstore.dto.user.UserRegistrationRequestDto;
import bookstore.dto.user.UserResponseDto;
import bookstore.dto.user.UserUpdateDto;
import bookstore.model.Role;
import bookstore.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserResponseDto register(UserRegistrationRequestDto requestDto);

    UserResponseDto addRole(String email, Role.RoleName roleName);

    UserResponseDto getCurrentUserInfo(User user);

    UserResponseDto update(UserUpdateDto updateDto, User user);
}

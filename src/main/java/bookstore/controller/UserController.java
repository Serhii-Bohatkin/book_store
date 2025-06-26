package bookstore.controller;

import bookstore.controller.api.UserControllerApi;
import bookstore.dto.user.UserResponseDto;
import bookstore.dto.user.UserUpdateDto;
import bookstore.model.Role;
import bookstore.model.User;
import bookstore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class UserController implements UserControllerApi {
    private final UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Override
    public UserResponseDto updateRolesByEmail(String email, Role.RoleName roleName) {
        return userService.addRole(email, roleName);
    }

    @Override
    public UserResponseDto getCurrentUserInfo(User user) {
        return userService.getCurrentUserInfo(user);
    }

    @Override
    public UserResponseDto updateUser(UserUpdateDto updateDto, User user) {
        return userService.update(updateDto, user);
    }
}

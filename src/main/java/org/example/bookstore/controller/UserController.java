package org.example.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bookstore.dto.user.UserResponseDto;
import org.example.bookstore.dto.user.UserUpdateDto;
import org.example.bookstore.model.Role;
import org.example.bookstore.model.User;
import org.example.bookstore.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users management", description = "Endpoints for managing users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update user role", description = "Update user role by email and role id")
    @PatchMapping("/{email}/role/{roleName}")
    public UserResponseDto updateRolesByEmail(@PathVariable String email,
                                              @PathVariable Role.RoleName roleName) {
        return userService.addRole(email, roleName);
    }

    @GetMapping("/me")
    @Operation(summary = "Get user information",
            description = "Get information about the current user")
    public UserResponseDto getCurrentUserInfo(@AuthenticationPrincipal User user) {
        return userService.getCurrentUserInfo(user);
    }

    @PatchMapping("/me")
    @Operation(summary = "Update user information",
            description = "Update information of the current user")
    public UserResponseDto updateUser(@RequestBody @Valid UserUpdateDto updateDto,
                                      @AuthenticationPrincipal User user) {
        return userService.update(updateDto, user);
    }
}

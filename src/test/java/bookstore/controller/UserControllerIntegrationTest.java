package bookstore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bookstore.TestObjectsFactory;
import bookstore.dto.user.UserResponseDto;
import bookstore.dto.user.UserUpdateDto;
import bookstore.model.Role;
import bookstore.model.User;
import bookstore.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.MessageFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {
        "classpath:database/clear-db.sql",
        "classpath:database/insert-users.sql",
        "classpath:database/insert-roles.sql",
        "classpath:database/link-users-to-roles.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
class UserControllerIntegrationTest {
    private static final String USER_NOT_FOUND_MESSAGE = "A user with email {0} does not exist";
    private static final String USER_ALREADY_HAS_ROLE_MESSAGE =
            "User with email %s already has the %s role";
    private static final String INVALID_FORMAT_EMAIL_MESSAGE = "email Invalid format email";
    private static final String FAILED_TO_CONVERT_ROLE_NAME_MESSAGE =
            "Failed to convert 'roleName' with value: '%s'";
    private static final String FILL_IN_AT_LEAST_ONE_FIELD_MESSAGE =
            "Please fill in at least one field";
    private static final String VALID_EMAIL = "user@gmail.com";
    private static final String ADMIN_ROLE_NAME = "ADMIN";
    private static final String BASE_URL = "/users";
    private static final String ROLE_PART_URL = "/role";
    private static final String ME_PART_URL = "/me";
    private static final String EMAIL_PARAM = "/{email}";
    private static final String ROLE_NAME_PARAM = "/{roleName}";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should add ADMIN role to user when valid email and role name are provided")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateRolesByEmail_ValidEmailAndRoleName_ShouldAddAdminRoleToTheUser() throws Exception {
        mockMvc.perform(put(BASE_URL + EMAIL_PARAM + ROLE_PART_URL + ROLE_NAME_PARAM,
                        VALID_EMAIL, ADMIN_ROLE_NAME))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        User userFromDB = userRepository.findByEmail(VALID_EMAIL).orElseThrow();
        assertThat(userFromDB.hasRole(Role.RoleName.valueOf(ADMIN_ROLE_NAME))).isTrue();
    }

    @Test
    @DisplayName("Should return 409 Conflict when user already has ADMIN role")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateRolesByEmail_UserIsAlreadyAdmin_Conflict() throws Exception {
        String adminEmail = "admin@gmail.com";

        String jsonResponse = mockMvc.perform(put(
                        BASE_URL + EMAIL_PARAM + ROLE_PART_URL + ROLE_NAME_PARAM,
                        adminEmail, ADMIN_ROLE_NAME))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(String.format(
                USER_ALREADY_HAS_ROLE_MESSAGE, adminEmail, ADMIN_ROLE_NAME));
    }

    @Test
    @DisplayName("Should return 404 Not Found when user with given email does not exist")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateRolesByEmail_NonExistingEmail_NotFound() throws Exception {
        String nonExistingEmail = "nonExisting@email.com";

        String jsonResponse = mockMvc.perform(put(
                        BASE_URL + EMAIL_PARAM + ROLE_PART_URL + ROLE_NAME_PARAM,
                        nonExistingEmail, ADMIN_ROLE_NAME))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                USER_NOT_FOUND_MESSAGE, nonExistingEmail));

    }

    @Test
    @DisplayName("Should return 400 Bad Request when email format is invalid")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateRolesByEmail_InvalidEmail_BadRequest() throws Exception {
        String jsonResponse = mockMvc.perform(put(
                        BASE_URL + EMAIL_PARAM + ROLE_PART_URL + ROLE_NAME_PARAM,
                        "invalidEmail", ADMIN_ROLE_NAME))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(INVALID_FORMAT_EMAIL_MESSAGE);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when role name is invalid")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateRolesByEmail_InvalidRoleName_BadRequest() throws Exception {
        String invalidRoleName = "invalidRoleName";

        String jsonResponse = mockMvc.perform(put(
                        BASE_URL + EMAIL_PARAM + ROLE_PART_URL + ROLE_NAME_PARAM,
                        VALID_EMAIL, invalidRoleName))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(String.format(
                FAILED_TO_CONVERT_ROLE_NAME_MESSAGE, invalidRoleName));
    }

    @Test
    @DisplayName("Should return current user info when authenticated")
    @WithUserDetails("user@gmail.com")
    void getCurrentUserInfo_ShouldReturnUserResponseDto() throws Exception {
        UserResponseDto expected = TestObjectsFactory.createUserResponseDto("user@gmail.com");

        String jsonResponse = mockMvc.perform(get(BASE_URL + ME_PART_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDto actual = objectMapper.readValue(jsonResponse, UserResponseDto.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should update user info when valid update request is sent")
    @WithUserDetails("user@gmail.com")
    void updateUser_ValidRequestDto_ShouldReturnUserResponseDto() throws Exception {
        String newFirstName = "newFirstName";
        UserUpdateDto userUpdateDto = TestObjectsFactory.createUserUpdateDto(newFirstName);
        String jsonRequest = objectMapper.writeValueAsString(userUpdateDto);

        String jsonResponse = mockMvc.perform(patch(BASE_URL + ME_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDto actual = objectMapper.readValue(jsonResponse, UserResponseDto.class);
        assertThat(actual.firstName()).isEqualTo(newFirstName);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when update request has no fields filled")
    @WithUserDetails("user@gmail.com")
    void updateUser_InvalidRequestDto_ShouldReturnUserResponseDto() throws Exception {
        UserUpdateDto emptyUpdateDto = new UserUpdateDto(null, null, null);
        String jsonRequest = objectMapper.writeValueAsString(emptyUpdateDto);

        String jsonResponse = mockMvc.perform(patch(BASE_URL + ME_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(FILL_IN_AT_LEAST_ONE_FIELD_MESSAGE);
    }
}

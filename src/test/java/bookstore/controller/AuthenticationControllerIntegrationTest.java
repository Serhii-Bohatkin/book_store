package bookstore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bookstore.TestObjectsFactory;
import bookstore.dto.jwt.JwtResponseDto;
import bookstore.dto.jwt.RefreshJwtRequestDto;
import bookstore.dto.user.UserLoginRequestDto;
import bookstore.dto.user.UserRegistrationRequestDto;
import bookstore.dto.user.UserResponseDto;
import bookstore.model.Role;
import bookstore.model.User;
import bookstore.security.AuthenticationService;
import bookstore.security.JwtService;
import bookstore.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.MessageFormat;
import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(scripts = {
        "classpath:database/clear-db.sql",
        "classpath:database/insert-users.sql",
        "classpath:database/insert-roles.sql",
        "classpath:database/link-users-to-roles.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
@Data
class AuthenticationControllerIntegrationTest {
    private static final String USER_ALREADY_EXISTS_MESSAGE =
            "A user with email {0} already exists";
    private static final String INCORRECT_INVITE_CODE_MESSAGE = "{0} is an incorrect invite code";
    private static final String INVALID_PASSWORD_MESSAGE =
            "password The password must be between 8 and 20 characters in length and have: 1) At "
                    + "least one capital English letter. 2) At least one lowercase English letter. "
                    + "3) At least one number. 4) At least one special character.";
    private static final String INVALID_EMAIL_MESSAGE = "email Invalid format email";
    private static final String BAD_CREDENTIALS_MESSAGE = "Bad credentials";
    private static final String PASSWORD_AND_REPEAT_PASSWORD_NOT_MATCHING_MESSAGE =
            "password and repeatPassword fields are not matching";
    private static final String MALFORMED_JWT_MESSAGE = "Malformed jwt";
    private static final String NEW_USER_EMAIL = "newUser@gmail.com";
    private static final String BASE_URL = "/auth";
    private static final String LOGIN_PART_URL = "/login";
    private static final String REGISTER_PART_URL = "/register";
    private static final String TOKEN_PART_URL = "/token";
    private static final String REFRESH_PART_URL = "/refresh";
    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.invite-code}")
    private String inviteCode;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Should return both access and refresh JWT tokens for a valid login request")
    void login_ValidRequestDto_ShouldReturnBothAccessAndRefreshJwtTokens() throws Exception {
        UserLoginRequestDto loginRequestDto = TestObjectsFactory.createUserLoginRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(loginRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL + LOGIN_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtResponseDto actual = objectMapper.readValue(jsonResponse, JwtResponseDto.class);
        assertThat(jwtService.validateAccessToken(actual.accessToken())).isTrue();
        assertThat(jwtService.validateRefreshToken(actual.refreshToken())).isTrue();
    }

    @Test
    @DisplayName("Should return 400 Bad Request for an invalid login request with null fields")
    void login_InvalidRequestDto_BadRequest() throws Exception {
        UserLoginRequestDto loginRequestDto = new UserLoginRequestDto(null, null);
        String jsonRequest = objectMapper.writeValueAsString(loginRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL + LOGIN_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(INVALID_PASSWORD_MESSAGE, INVALID_EMAIL_MESSAGE);
    }

    @Test
    @DisplayName("Should return 403 Forbidden for login with incorrect password")
    void login_IncorrectPassword_Forbidden() throws Exception {
        UserLoginRequestDto loginRequestDto =
                new UserLoginRequestDto("user@gmail.com", "wrongPass1234!");
        String jsonRequest = objectMapper.writeValueAsString(loginRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL + LOGIN_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(BAD_CREDENTIALS_MESSAGE);
    }

    @Test
    @DisplayName("Should register a new user and return UserResponseDto for a valid request")
    void register_ValidRequestDto_ShouldReturnUserResponseDto() throws Exception {
        UserRegistrationRequestDto registrationRequestDto
                = TestObjectsFactory.createRegistrationRequestDto(NEW_USER_EMAIL, null);
        UserResponseDto expected =
                TestObjectsFactory.createUserResponseDto(NEW_USER_EMAIL);
        String jsonRequest = objectMapper.writeValueAsString(registrationRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL + REGISTER_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDto actual = objectMapper.readValue(jsonResponse, UserResponseDto.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("userId")
                .isEqualTo(expected);
        User user = (User) userService.loadUserByUsername(NEW_USER_EMAIL);
        assertThat(user.hasRole(Role.RoleName.USER)).isTrue();
        assertThat(user.hasRole(Role.RoleName.ADMIN)).isFalse();
    }

    @Test
    @DisplayName("Should return 400 Bad Request when user already exists")
    void register_UserAlreadyExists_BadRequest() throws Exception {
        String userEmail = "user@gmail.com";
        UserRegistrationRequestDto registrationRequestDto
                = TestObjectsFactory.createRegistrationRequestDto(userEmail, null);
        String jsonRequest = objectMapper.writeValueAsString(registrationRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL + REGISTER_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                USER_ALREADY_EXISTS_MESSAGE, userEmail));
    }

    @Test
    @DisplayName("Should register a new admin user when a valid invite code is provided")
    @Sql(scripts = {"classpath:database/clear-users_roles.sql",
            "classpath:database/clear-users.sql"}, executionPhase = BEFORE_TEST_METHOD)
    void register_AdminRegistrationValidInviteCode_ShouldReturnUserResponseDto() throws Exception {
        UserRegistrationRequestDto registrationRequestDto
                = TestObjectsFactory.createRegistrationRequestDto(adminEmail, inviteCode);
        UserResponseDto expected = TestObjectsFactory.createUserResponseDto(adminEmail);
        String jsonRequest = objectMapper.writeValueAsString(registrationRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL + REGISTER_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponseDto actual = objectMapper.readValue(jsonResponse, UserResponseDto.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("userId")
                .isEqualTo(expected);
        User user = (User) userService.loadUserByUsername(adminEmail);
        assertThat(user.hasRole(Role.RoleName.USER)).isTrue();
        assertThat(user.hasRole(Role.RoleName.ADMIN)).isTrue();
    }

    @Test
    @DisplayName("Should return 400 Bad Request when an invalid admin invite code is provided")
    @Sql(scripts = {"classpath:database/clear-users_roles.sql",
            "classpath:database/clear-users.sql"}, executionPhase = BEFORE_TEST_METHOD)
    void register_AdminRegistrationInvalidInviteCode_BadRequest() throws Exception {
        String wrongInviteCode = "wrongInviteCode";
        UserRegistrationRequestDto registrationRequestDto
                = TestObjectsFactory.createRegistrationRequestDto(adminEmail, wrongInviteCode);
        String jsonRequest = objectMapper.writeValueAsString(registrationRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL + REGISTER_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                INCORRECT_INVITE_CODE_MESSAGE, wrongInviteCode));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for an invalid registration request")
    void register_InvalidRequestDto_BadRequest() throws Exception {
        UserRegistrationRequestDto registrationRequestDto
                = TestObjectsFactory.createInvalidRegistrationRequestDto();

        String jsonRequest = objectMapper.writeValueAsString(registrationRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL + REGISTER_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(PASSWORD_AND_REPEAT_PASSWORD_NOT_MATCHING_MESSAGE,
                INVALID_EMAIL_MESSAGE, INVALID_PASSWORD_MESSAGE);
    }

    @Test
    @DisplayName("Should return a new access token for a valid refresh JWT")
    void getNewAccessToken_ValidRefreshJwt_ShouldReturnOnlyNewAccessJwt() throws Exception {
        RefreshJwtRequestDto refreshJwtRequestDto = new RefreshJwtRequestDto(getRefreshToken());
        String jsonRequest = objectMapper.writeValueAsString(refreshJwtRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL + TOKEN_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtResponseDto actual = objectMapper.readValue(jsonResponse, JwtResponseDto.class);
        assertThat(jwtService.validateAccessToken(actual.accessToken())).isTrue();
        assertThat(actual.refreshToken()).isNull();
    }

    @Test
    @DisplayName("Should return 400 Bad Request for an invalid refresh JWT")
    @WithUserDetails("user@gmail.com")
    void getNewRefreshToken_InvalidRefreshJwt_BadRequest() throws Exception {
        RefreshJwtRequestDto refreshJwtRequestDto = new RefreshJwtRequestDto("invalidToken");
        String jsonRequest = objectMapper.writeValueAsString(refreshJwtRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL + REFRESH_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MALFORMED_JWT_MESSAGE);
    }

    @Test
    @DisplayName("Should return both new access and refresh tokens for a valid refresh JWT")
    @WithUserDetails("user@gmail.com")
    void getNewRefreshToken_ValidRefreshJwt_ShouldReturnBothNewAccessAndRefreshJwtTokens()
            throws Exception {
        RefreshJwtRequestDto refreshJwtRequestDto = new RefreshJwtRequestDto(getRefreshToken());
        String jsonRequest = objectMapper.writeValueAsString(refreshJwtRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL + REFRESH_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtResponseDto actual = objectMapper.readValue(jsonResponse, JwtResponseDto.class);
        assertThat(jwtService.validateAccessToken(actual.accessToken())).isTrue();
        assertThat(jwtService.validateRefreshToken(actual.refreshToken())).isTrue();
    }

    @Test
    @DisplayName(
            "Should return 403 Forbidden when requesting new refresh token with invalid access JWT")
    void getNewRefreshToken_InvalidAccessJwt_Forbidden() throws Exception {
        RefreshJwtRequestDto refreshJwtRequestDto = new RefreshJwtRequestDto(getRefreshToken());
        String jsonRequest = objectMapper.writeValueAsString(refreshJwtRequestDto);

        mockMvc.perform(post(BASE_URL + REFRESH_PART_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    private String getRefreshToken() {
        UserLoginRequestDto userLoginRequestDto = TestObjectsFactory.createUserLoginRequestDto();
        JwtResponseDto jwtResponseDto = authenticationService.login(userLoginRequestDto);
        return jwtResponseDto.refreshToken();
    }
}

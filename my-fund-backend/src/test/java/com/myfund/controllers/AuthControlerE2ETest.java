package com.myfund.controllers;

import com.myfund.models.DTOs.CreateUserDTO;
import com.myfund.models.DTOs.PasswordChangeDTO;
import com.myfund.models.DTOs.PasswordChangeRequestDTO;
import com.myfund.models.DTOs.UserDTO;
import com.myfund.services.UserService;
import com.myfund.services.email.EmailSender;
import com.myfund.services.email.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControlerE2ETest {

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.26").withDatabaseName("testdb").withUsername("testuser").withPassword("testpass");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private EmailSender emailSender;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeAll
    public void setUp() throws IOException {
        mysqlContainer.start();
        Mockito.doNothing().when(emailSender).sendWelcomeEmail(Mockito.any());
    }

    @AfterEach
    public void cleanUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("TRUNCATE TABLE budget");
        jdbcTemplate.execute("TRUNCATE TABLE expense");
        jdbcTemplate.execute("TRUNCATE TABLE income");
        jdbcTemplate.execute("TRUNCATE TABLE category");
        jdbcTemplate.execute("TRUNCATE TABLE subcategory");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Test
    public void testRegisterUser() {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setPassword("password");
        createUserDTO.setEmail("test@test.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateUserDTO> request = new HttpEntity<>(createUserDTO, headers);

        ResponseEntity<UserDTO> response = restTemplate.postForEntity("http://localhost:" + port + "/register", request, UserDTO.class);
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getEmail()).isEqualTo("test@test.com");
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testRequestChangePassword() {
        PasswordChangeRequestDTO passwordChangeRequestDTO = new PasswordChangeRequestDTO();
        passwordChangeRequestDTO.setEmail("test@example.com");

        ResponseEntity<?> response = restTemplate.postForEntity("http://localhost:" + port + "/request-change-password", passwordChangeRequestDTO, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void testChangePassword() throws IOException {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setPassword("oldPassword");
        createUserDTO.setEmail("test@example.com");
        userService.createUser(createUserDTO);

        String passwordResetToken = tokenService.createPasswordResetToken("test@example.com");

        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setEmail("test@example.com");
        passwordChangeDTO.setToken(passwordResetToken);
        passwordChangeDTO.setNewPassword("newPassword");

        ResponseEntity<?> response = restTemplate.postForEntity("http://localhost:" + port + "/change-password", passwordChangeDTO, Void.class);
        String newPassword = jdbcTemplate.queryForObject("SELECT password FROM users WHERE id = 1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(passwordEncoder.matches(passwordChangeDTO.getNewPassword(), newPassword)).isTrue();
    }

    @Test
    public void testRegisterUserWithExistingUsername() {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setPassword("password");
        createUserDTO.setEmail("test@test.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateUserDTO> request = new HttpEntity<>(createUserDTO, headers);

        ResponseEntity<UserDTO> initialResponse = restTemplate.postForEntity("http://localhost:" + port + "/register", request, UserDTO.class);
        ResponseEntity<Map> duplicateResponse = restTemplate.postForEntity("http://localhost:" + port + "/register", request, Map.class);
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Map<String, String> errorResponse = (Map<String, String>) duplicateResponse.getBody();

        assertThat(initialResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.get("message")).isEqualTo("Username is not unique");
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testRequestChangePasswordWithInvalidEmail() throws IOException {
        PasswordChangeRequestDTO passwordChangeRequestDTO = new PasswordChangeRequestDTO();
        passwordChangeRequestDTO.setEmail("invalid#example.com");

        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:" + port + "/request-change-password", passwordChangeRequestDTO, Map.class);
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();

        assertThat(errorResponse.get("email")).isEqualTo("Invalid email format");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testChangePasswordWithInvalidToken() throws IOException {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setPassword("oldPassword");
        createUserDTO.setEmail("test@example.com");
        userService.createUser(createUserDTO);

        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setEmail("test@example.com");
        passwordChangeDTO.setToken("invalidToken");
        passwordChangeDTO.setNewPassword("newPassword");

        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:" + port + "/change-password", passwordChangeDTO, Map.class);
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        String currentPassword = jdbcTemplate.queryForObject("SELECT password FROM users WHERE id = 1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(passwordEncoder.matches(createUserDTO.getPassword(), currentPassword)).isTrue();
        assertThat(errorResponse.get("message")).isEqualTo("Invalid token");
    }

    @Test
    public void testRegisterUserWithInvalidUsername() {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("us");
        createUserDTO.setPassword("password");
        createUserDTO.setEmail("test@test.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateUserDTO> request = new HttpEntity<>(createUserDTO, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:" + port + "/register", request, Map.class);
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(count).isEqualTo(0);
        assertThat(errorResponse.get("username")).isEqualTo("User name must be at least 4 characters long");
    }

    @Test
    public void testRegisterUserWithInvalidPassword() {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setPassword("pwd");
        createUserDTO.setEmail("test@test.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateUserDTO> request = new HttpEntity<>(createUserDTO, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:" + port + "/register", request, Map.class);
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(count).isEqualTo(0);
        assertThat(errorResponse.get("password")).isEqualTo("Password must be at least 4 characters long");
    }
}
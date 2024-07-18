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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControlerE2ETest {

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.26")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }
}
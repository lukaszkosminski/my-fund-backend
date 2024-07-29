package com.myfund.controllers;

import com.myfund.models.DTOs.ApplicationDetailsDTO;
import com.myfund.services.ApplicationDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class VersionControllerE2ETest {

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.26").withDatabaseName("testdb").withUsername("testuser").withPassword("testpass");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationDetailsService applicationDetailsService;

    @BeforeEach
    public void setUp() {
        mysqlContainer.start();
    }

    @Test
    public void testGetVersion_Success() throws Exception {
        ApplicationDetailsDTO version = applicationDetailsService.getVersion();

        mockMvc.perform(get("/version")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(version.getVersion()))
                .andExpect(jsonPath("$.buildDate").value(version.getBuildDate()));
    }
}
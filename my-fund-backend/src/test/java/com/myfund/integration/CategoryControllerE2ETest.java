package com.myfund.integration;

import com.myfund.models.Category;
import com.myfund.models.SubCategory;
import com.myfund.models.User;
import com.myfund.repositories.CategoryRepository;
import com.myfund.repositories.SubCategoryRepository;
import com.myfund.repositories.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class CategoryControllerE2ETest {

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.26").withDatabaseName("testdb").withUsername("testuser").withPassword("testpass");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @BeforeEach
    public void setUp() {
        mysqlContainer.start();

        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User("testUser", "testPassword", new ArrayList<>());

        User customUser = User.builder()
                .id(1L)
                .username(springUser.getUsername())
                .password("testPassword")
                .role("USER")
                .email("test@example.com")
                .build();
        userRepository.save(customUser);

        SecurityContextHolder.getContext().setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                customUser, null, springUser.getAuthorities()));
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
    public void testGetAllCategories() throws Exception {

        Optional<User> optionalUser = userRepository.findById(1L);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("User not found");
        }
        User user = optionalUser.get();

        Category category = Category.builder()
                .name("Test Category 1")
                .user(user)
                .subCategories(new ArrayList<>())
                .build();
        categoryRepository.save(category);

        Category category2 = Category.builder()
                .name("Test Category 2")
                .user(user)
                .subCategories(new ArrayList<>())
                .build();
        categoryRepository.save(category2);

        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Category 1"))
                .andExpect(jsonPath("$[1].name").value("Test Category 2"));
    }

    @Test
    public void testGetAllCategories_Unauthorized() throws Exception {
        // Clear the security context to simulate an unauthorized request
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetCategoryById_Success() throws Exception {
        Optional<User> optionalUser = userRepository.findById(1L);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("User not found");
        }
        User user = optionalUser.get();

        Category category = Category.builder()
                .name("Test Category")
                .user(user)
                .subCategories(new ArrayList<>())
                .build();
        categoryRepository.save(category);

        mockMvc.perform(get("/api/categories/" + category.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Category"));
    }

    @Test
    public void testGetCategoryById_NotFound() throws Exception {
        mockMvc.perform(get("/api/categories/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void testGetCategoryById_Unauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateCategory_Success() throws Exception {
        String createCategoryJson = "{\"name\":\"New Category\"}";

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createCategoryJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Category"));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM category", Integer.class);
        assertEquals(1, count);
    }

    @Test
    public void testCreateCategory_MissingName() throws Exception {
        String createCategoryJson = "{}";

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createCategoryJson))
                .andExpect(status().isBadRequest());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM category", Integer.class);
        assertEquals(0, count);
    }

    @Test
    public void testCreateCategory_Unauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        String createCategoryJson = "{\"name\":\"New Category\"}";

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createCategoryJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateCategory_Success() throws Exception {
        Optional<User> optionalUser = userRepository.findById(1L);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("User not found");
        }
        User user = optionalUser.get();

        Category category = Category.builder()
                .name("Old Category")
                .user(user)
                .subCategories(new ArrayList<>())
                .build();
        Category savedCategory = categoryRepository.save(category);

        String updateCategoryJson = "{\"name\":\"Updated Category\", \"subCategories\":[{\"name\":\"SubCategory1\"}, {\"name\":\"SubCategory2\"}]}";

        mockMvc.perform(patch("/api/categories/" + savedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateCategoryJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category"));

        Category updatedCategory = categoryRepository.findById(category.getId()).orElseThrow(() -> new RuntimeException("Category not found"));
        assertEquals("Updated Category", updatedCategory.getName());
    }

    @Test
    public void testUpdateCategory_NotFound() throws Exception {
        String updateCategoryJson = "{\"name\":\"Updated Category\"}";

        mockMvc.perform(patch("/api/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateCategoryJson))
                .andExpect(status().isConflict());
    }

    @Test
    public void testUpdateCategory_Unauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        String updateCategoryJson = "{\"name\":\"Updated Category\"}";

        mockMvc.perform(patch("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateCategoryJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteCategory_Success() throws Exception {
        Optional<User> optionalUser = userRepository.findById(1L);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("User not found");
        }
        User user = optionalUser.get();

        Category category = Category.builder()
                .name("Category to Delete")
                .user(user)
                .subCategories(new ArrayList<>())
                .build();
        Category savedCategory = categoryRepository.save(category);

        mockMvc.perform(delete("/api/categories/" + savedCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        Optional<Category> deletedCategory = categoryRepository.findById(savedCategory.getId());
        assertFalse(deletedCategory.isPresent(), "The category should be deleted");
    }

    @Test
    public void testDeleteCategory_NotFound() throws Exception {
        mockMvc.perform(delete("/api/categories/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void testDeleteSubcategory_Success() throws Exception {
        Optional<User> optionalUser = userRepository.findById(1L);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("User not found");
        }
        User user = optionalUser.get();

        Category category = Category.builder()
                .name("Test Category 1")
                .user(user)
                .subCategories(new ArrayList<>())
                .build();

        SubCategory subCategory = SubCategory.builder().name("SubCategory1").category(category).build();

        category.getSubCategories().add(subCategory);

        Category savedCategory = categoryRepository.save(category);
        SubCategory savedSubCategory = savedCategory.getSubCategories().get(0);

        mockMvc.perform(delete("/api/categories/" + savedCategory.getId() + "/subcategories/" + savedSubCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        Optional<SubCategory> deletedSubCategory = subCategoryRepository.findById(savedSubCategory.getId());
        assertFalse(deletedSubCategory.isPresent(), "The subcategory should be deleted");
    }

    @Test
    public void testDeleteSubcategory_NotFound() throws Exception {
        mockMvc.perform(delete("/api/categories/1/subcategories/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}
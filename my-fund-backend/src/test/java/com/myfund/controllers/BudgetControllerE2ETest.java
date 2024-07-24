package com.myfund.controllers;

import com.myfund.models.Budget;
import com.myfund.models.User;
import com.myfund.repositories.BudgetRepository;
import com.myfund.repositories.IncomeRepository;
import com.myfund.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class BudgetControllerE2ETest {

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.26").withDatabaseName("testdb").withUsername("testuser").withPassword("testpass");


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @WithMockUser(username = "testUser", roles = {"USER"})
    @BeforeEach
    public void setUp(){
        mysqlContainer.start();

        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User("testUser", "testPassword", new ArrayList<>());

        User customUser = new User();
        customUser.setId(1L);
        customUser.setUsername(springUser.getUsername());
        customUser.setPassword("testPassword");
        customUser.setRole("USER");
        customUser.setEmail("test@example.com");
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
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Test
    public void createBudget_Success() throws Exception {

        String createBudgetJson = "{\"name\":\"Test Budget\"}";

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBudgetJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"name\":\"Test Budget\"}"));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM budget", Integer.class);
        Assertions.assertEquals(1, count);
    }

    @Test
    public void createBudget_MissingName() throws Exception {
        String createBudgetJson = "{}";

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBudgetJson))
                .andExpect(status().isBadRequest());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM budget", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void createBudget_InvalidJson() throws Exception {
        String createBudgetJson = "{\"invalidField\":\"value\"}";

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBudgetJson))
                .andExpect(status().isBadRequest());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM budget", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void createBudget_Unauthorized() throws Exception {
        String createBudgetJson = "{\"name\":\"Test Budget\"}";

        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBudgetJson))
                .andExpect(status().isUnauthorized());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM budget", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void createBudget_NameAlreadyExists_ShouldThrowException() throws Exception {
        String createBudgetJson = "{\"name\":\"Test Budget\"}"; // Name already exists for the user

        // Save a budget with the same name for the user
        Budget existingBudget = new Budget();
        existingBudget.setName("Test Budget");
        existingBudget.setUser(userRepository.findById(1L).get());
        budgetRepository.save(existingBudget);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBudgetJson))
                .andExpect(status().isConflict());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM budget", Integer.class);
        Assertions.assertEquals(1, count);
    }

    @Test
    public void getBudgetById_Success() throws Exception {
        String expectedBudgetJson = "{\"id\":1, \"name\":\"Test Budget\"}";

        Long budgetId = 1L;
        Budget budget = new Budget();
        budget.setId(budgetId);
        budget.setName("Test Budget");
        budget.setUser(userRepository.findById(1L).get());
        budgetRepository.save(budget);

        mockMvc.perform(get("/api/budgets/" + budgetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedBudgetJson));
    }

    @Test
    public void getBudgetById_NotFound() throws Exception {
        Long nonExistentBudgetId = 999L;

        mockMvc.perform(get("/api/budgets/" + nonExistentBudgetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void getBudgetById_Unauthorized() throws Exception {
        Long budgetId = 1L;
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/budgets/" + budgetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAllBudgets_Success() throws Exception {
        String expectedBudgetsJson = "[{\"id\":1, \"name\":\"Test Budget 1\"}, {\"id\":2, \"name\":\"Test Budget 2\"}]"; // Replace with the expected JSON representation of the budgets
        User user = userRepository.findById(1L).get();

        Budget budget1 = new Budget();
        budget1.setName("Test Budget 1");
        budget1.setUser(user);

        Budget budget2 = new Budget();
        budget2.setName("Test Budget 2");
        budget2.setUser(user);

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);

        mockMvc.perform(get("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedBudgetsJson));
    }

    @Test
    public void getAllBudgets_Unauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void createExpense_Success() throws Exception {
        String createExpenseJson = "{\"amount\": \"100.0\", \"name\": \"Test Expense\"}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = new Budget();
        budget1.setName("Test Budget 1");
        budget1.setUser(user);
        budget1.setBalance(BigDecimal.ZERO);
        budget1.setTotalExpense(BigDecimal.ZERO);
        budget1.setTotalIncome(BigDecimal.ZERO);
        Budget savedBudget = budgetRepository.save(budget1);
        Long budgetId = savedBudget.getId();
        mockMvc.perform(post("/api/budgets/" + budgetId + "/expenses", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createExpenseJson))
                .andExpect(status().isCreated());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expense", Integer.class);
        Assertions.assertEquals(1, count);
    }

    @Test
    public void createExpense_InvalidAmount_ShouldThrowException() throws Exception {
        String createExpenseJson = "{\"amount\": \"-100.0\", \"name\": \"Test Expense\"}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = new Budget();
        budget1.setName("Test Budget 1");
        budget1.setUser(user);
        budget1.setBalance(BigDecimal.ZERO);
        budget1.setTotalExpense(BigDecimal.ZERO);
        budget1.setTotalIncome(BigDecimal.ZERO);
        Budget savedBudget = budgetRepository.save(budget1);
        Long budgetId = savedBudget.getId();
        mockMvc.perform(post("/api/budgets/" + budgetId + "/expenses", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createExpenseJson))
                .andExpect(status().isBadRequest());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expense", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void createExpense_MissingName_ShouldThrowException() throws Exception {
        String createExpenseJson = "{\"amount\": \"100.0\"}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = new Budget();
        budget1.setName("Test Budget 1");
        budget1.setUser(user);
        budget1.setBalance(BigDecimal.ZERO);
        budget1.setTotalExpense(BigDecimal.ZERO);
        budget1.setTotalIncome(BigDecimal.ZERO);
        Budget savedBudget = budgetRepository.save(budget1);
        Long budgetId = savedBudget.getId();
        mockMvc.perform(post("/api/budgets/" + budgetId + "/expenses", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createExpenseJson))
                .andExpect(status().isBadRequest());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expense", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void createExpense_InvalidJson_ShouldThrowException() throws Exception {
        String createExpenseJson = "{\"invalidField\": \"100.0\", \"name\": \"Test Expense\"}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = new Budget();
        budget1.setName("Test Budget 1");
        budget1.setUser(user);
        budget1.setBalance(BigDecimal.ZERO);
        budget1.setTotalExpense(BigDecimal.ZERO);
        budget1.setTotalIncome(BigDecimal.ZERO);
        Budget savedBudget = budgetRepository.save(budget1);
        Long budgetId = savedBudget.getId();
        mockMvc.perform(post("/api/budgets/" + budgetId + "/expenses", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createExpenseJson))
                .andExpect(status().isBadRequest());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expense", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void createExpense_Unauthorized_ShouldThrowException() throws Exception {
        String createExpenseJson = "{\"amount\": \"100.0\", \"name\": \"Test Expense\"}";

        SecurityContextHolder.clearContext();

        User user = userRepository.findById(1L).get();

        Budget budget1 = new Budget();
        budget1.setName("Test Budget 1");
        budget1.setUser(user);
        budget1.setBalance(BigDecimal.ZERO);
        budget1.setTotalExpense(BigDecimal.ZERO);
        budget1.setTotalIncome(BigDecimal.ZERO);
        Budget savedBudget = budgetRepository.save(budget1);
        Long budgetId = savedBudget.getId();
        SecurityContextHolder.clearContext();
        mockMvc.perform(post("/api/budgets/" + budgetId + "/expenses", budgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createExpenseJson))
                .andExpect(status().isUnauthorized());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expense", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void createExpense_BudgetNotFound_ShouldThrowException() throws Exception {
        String createExpenseJson = "{\"amount\": \"100.0\", \"name\": \"Test Expense\"}";

        Long nonExistentBudgetId = 999L;

        mockMvc.perform(post("/api/budgets/" + nonExistentBudgetId + "/expenses", nonExistentBudgetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createExpenseJson))
                .andExpect(status().isConflict());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expense", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void createIncome_Success() throws Exception {
        String createIncomeJson = "{\"name\":\"Test Income\", \"amount\": \"100.0\"}";

        Long budgetId = 1L;
        Budget budget = new Budget();
        budget.setId(budgetId);
        budget.setName("Test Budget");
        budget.setBalance(BigDecimal.ZERO);
        budget.setTotalExpense(BigDecimal.ZERO);
        budget.setTotalIncome(BigDecimal.ZERO);
        budget.setUser(userRepository.findById(1L).get());
        budgetRepository.save(budget);

        mockMvc.perform(post("/api/budgets/" + budgetId + "/incomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createIncomeJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"id\":1, \"name\":\"Test Income\", \"amount\":100.0}"));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM income", Integer.class);
        Assertions.assertEquals(1, count);

    }

    @Test
    public void createIncome_InvalidInput_ShouldThrowException() throws Exception {
        String createIncomeJson = "{\"name\":\"Test Income\", \"amount\": -100.0, \"categoryId\": 1, \"subcategoryId\": 1}";

        Long budgetId = 1L;
        Budget budget = new Budget();
        budget.setId(budgetId);
        budget.setBalance(BigDecimal.ZERO);
        budget.setTotalExpense(BigDecimal.ZERO);
        budget.setTotalIncome(BigDecimal.ZERO);
        budget.setName("Test Budget");
        budget.setUser(userRepository.findById(1L).get());
        budgetRepository.save(budget);

        mockMvc.perform(post("/api/budgets/" + budgetId + "/incomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createIncomeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"message\": \"Amount cannot be negative\"}"));
    }

    @Test
    public void createIncome_NotFoundBudget_ShouldThrowException() throws Exception {
        String createIncomeJson = "{\"name\":\"Test Income\", \"amount\": 100.0, \"categoryId\": 1, \"subcategoryId\": 1}";

        Long nonExistentBudgetId = 999L;

        mockMvc.perform(post("/api/budgets/" + nonExistentBudgetId + "/incomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createIncomeJson))
                .andExpect(status().isConflict());
    }

    @Test
    public void createIncome_Unauthorized_ShouldReturnUnauthorized() throws Exception {
        String createIncomeJson = "{\"name\":\"Test Income\", \"amount\": 100.0, \"categoryId\": 1, \"subcategoryId\": 1}";

        Long budgetId = 1L;
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/budgets/" + budgetId + "/incomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createIncomeJson))
                .andExpect(status().isUnauthorized());
    }
}
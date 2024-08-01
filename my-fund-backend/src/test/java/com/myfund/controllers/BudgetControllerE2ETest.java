package com.myfund.controllers;

import com.myfund.exceptions.InvalidInputException;
import com.myfund.models.*;
import com.myfund.models.DTOs.CreateBudgetDTO;
import com.myfund.models.DTOs.CreateExpenseDTO;
import com.myfund.models.DTOs.CreateIncomeDTO;
import com.myfund.repositories.*;
import com.myfund.services.BudgetService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
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
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BudgetService budgetService;

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
        jdbcTemplate.execute("TRUNCATE TABLE category");
        jdbcTemplate.execute("TRUNCATE TABLE subcategory");
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

        mockMvc.perform(post("/api/budgets/" + budgetId + "/incomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createIncomeJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateExpense_Success() throws Exception {
        String updateExpenseJson = "{\"name\":\"Updated Expense\", \"amount\": \"200.0\"}";

        User user = userRepository.findById(1L).get();
        Long budgetId = 1L;
        Long expenseId = 1L;

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setId(budgetId);
        budget.setName("Test Budget");
        budget.setBalance(BigDecimal.ZERO);
        budget.setTotalExpense(BigDecimal.ZERO);
        budget.setTotalIncome(BigDecimal.ZERO);
        budgetRepository.save(budget);

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setBudget(budget);
        expense.setId(expenseId);
        expense.setName("Test Expense");
        expense.setAmount(BigDecimal.valueOf(100.0));
        expense.setBudget(budget);
        expenseRepository.save(expense);

        mockMvc.perform(patch("/api/budgets/" + budgetId + "/expenses/" + expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpenseJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"id\":1, \"name\":\"Updated Expense\", \"amount\":200.0}"));

        Expense updatedExpense = expenseRepository.findById(expenseId).get();
        Assertions.assertEquals("Updated Expense", updatedExpense.getName());
        Assertions.assertEquals(BigDecimal.valueOf(200.0), updatedExpense.getAmount());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expense", Integer.class);
        Assertions.assertEquals(1, count);
    }

    @Test
    public void updateExpense_InvalidAmount_ShouldThrowException() throws Exception {
        String updateExpenseJson = "{\"name\":\"Updated Expense\", \"amount\": \"-200.0\"}";

        User user = userRepository.findById(1L).get();
        Long budgetId = 1L;
        Long expenseId = 1L;

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setId(budgetId);
        budget.setName("Test Budget");
        budget.setBalance(BigDecimal.ZERO);
        budget.setTotalExpense(BigDecimal.ZERO);
        budget.setTotalIncome(BigDecimal.ZERO);
        budgetRepository.save(budget);

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setBudget(budget);
        expense.setId(expenseId);
        expense.setName("Test Expense");
        expense.setAmount(BigDecimal.valueOf(100.0));
        expense.setBudget(budget);
        expenseRepository.save(expense);

        mockMvc.perform(patch("/api/budgets/" + budgetId + "/expenses/" + expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpenseJson))
                .andExpect(status().isBadRequest());

        Expense updatedExpense = expenseRepository.findById(expenseId).get();
        Assertions.assertEquals("Test Expense", updatedExpense.getName());
        Assertions.assertEquals(BigDecimal.valueOf(100.0), updatedExpense.getAmount());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expense", Integer.class);
        Assertions.assertEquals(1, count);
    }

    @Test
    public void updateExpense_NotFoundExpense_ShouldThrowException() throws Exception {
        String updateExpenseJson = "{\"name\":\"Updated Expense\", \"amount\": \"200.0\"}";

        Long budgetId = 1L;
        Long nonExistentExpenseId = 999L;

        User user = userRepository.findById(1L).get();
        Long expenseId = 1L;

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setId(budgetId);
        budget.setName("Test Budget");
        budget.setBalance(BigDecimal.ZERO);
        budget.setTotalExpense(BigDecimal.ZERO);
        budget.setTotalIncome(BigDecimal.ZERO);
        budgetRepository.save(budget);

        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setUser(user);
        expense.setBudget(budget);
        expense.setName("Test Expense");
        expense.setAmount(BigDecimal.valueOf(100.0));
        expenseRepository.save(expense);


        mockMvc.perform(patch("/api/budgets/" + budgetId + "/expenses/" + nonExistentExpenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpenseJson))
                .andExpect(status().isConflict());
    }

    @Test
    public void updateExpense_Unauthorized_ShouldReturnUnauthorized() throws Exception {
        String updateExpenseJson = "{\"name\":\"Updated Expense\", \"amount\": \"200.0\"}";

        Long budgetId = 1L;
        Long expenseId = 1L;

        SecurityContextHolder.clearContext();

        mockMvc.perform(patch("/api/budgets/" + budgetId + "/expenses/" + expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpenseJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateIncome_Success() throws Exception {
        String updateIncomeJson = "{\"amount\": \"200.0\", \"name\": \"Updated Income\"}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = new Budget();
        budget1.setName("Test Budget 1");
        budget1.setUser(user);
        budget1.setBalance(BigDecimal.ZERO);
        budget1.setTotalExpense(BigDecimal.ZERO);
        budget1.setTotalIncome(BigDecimal.ZERO);
        Budget savedBudget = budgetRepository.save(budget1);

        Income income = new Income();
        income.setBudget(savedBudget);
        income.setName("Test Income");
        income.setAmount(BigDecimal.valueOf(100.0));
        income.setUser(user);
        Income savedIncome = incomeRepository.save(income);
        Long incomeId = savedIncome.getId();
        Long budgetId = savedBudget.getId();

        mockMvc.perform(patch("/api/budgets/" + budgetId + "/incomes/" + incomeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateIncomeJson))
                .andExpect(status().isOk());

        Income updatedIncome = incomeRepository.findById(incomeId).get();
        Assertions.assertEquals("Updated Income", updatedIncome.getName());
        Assertions.assertEquals(BigDecimal.valueOf(200.0), updatedIncome.getAmount());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM income", Integer.class);
        Assertions.assertEquals(1, count);
    }

    @Test
    public void updateIncome_InvalidAmount_ShouldThrowException() throws Exception {
        String updateIncomeJson = "{\"amount\": \"-200.0\", \"name\": \"Updated Income\"}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = new Budget();
        budget1.setName("Test Budget 1");
        budget1.setUser(user);
        budget1.setBalance(BigDecimal.ZERO);
        budget1.setTotalExpense(BigDecimal.ZERO);
        budget1.setTotalIncome(BigDecimal.ZERO);
        Budget savedBudget = budgetRepository.save(budget1);

        Income income = new Income();
        income.setName("Test Income");
        income.setAmount(BigDecimal.valueOf(100.0));
        income.setBudget(savedBudget);
        income.setUser(user);
        Income savedIncome = incomeRepository.save(income);
        Long incomeId = savedIncome.getId();
        Long budgetId = savedBudget.getId();

        mockMvc.perform(patch("/api/budgets/" + budgetId + "/incomes/" + incomeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateIncomeJson))
                .andExpect(status().isBadRequest());

        Income updatedIncome = incomeRepository.findById(incomeId).get();
        Assertions.assertEquals("Test Income", updatedIncome.getName());
        Assertions.assertEquals(BigDecimal.valueOf(100.0), updatedIncome.getAmount());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM income", Integer.class);
        Assertions.assertEquals(1, count);
    }

    @Test
    public void updateIncome_MissingName_ShouldThrowException() throws Exception {
        String updateIncomeJson = "{\"amount\": \"200.0\"}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = new Budget();
        budget1.setName("Test Budget 1");
        budget1.setUser(user);
        budget1.setBalance(BigDecimal.ZERO);
        budget1.setTotalExpense(BigDecimal.ZERO);
        budget1.setTotalIncome(BigDecimal.ZERO);
        Budget savedBudget = budgetRepository.save(budget1);

        Income income = new Income();
        income.setName("Test Income");
        income.setAmount(BigDecimal.valueOf(100.0));
        income.setBudget(savedBudget);
        income.setUser(user);
        Income savedIncome = incomeRepository.save(income);
        Long incomeId = savedIncome.getId();
        Long budgetId = savedBudget.getId();

        mockMvc.perform(patch("/api/budgets/" + budgetId + "/incomes/" + incomeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateIncomeJson))
                .andExpect(status().isBadRequest());

        Income updatedIncome = incomeRepository.findById(incomeId).get();
        Assertions.assertEquals("Test Income", updatedIncome.getName());
        Assertions.assertEquals(BigDecimal.valueOf(100.0), updatedIncome.getAmount());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM income", Integer.class);
        Assertions.assertEquals(1, count);
    }

    @Test
    public void updateIncome_NotFound_ShouldThrowException() throws Exception {
        String updateIncomeJson = "{\"amount\": \"200.0\", \"name\": \"Updated Income\"}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = new Budget();
        budget1.setName("Test Budget 1");
        budget1.setUser(user);
        budget1.setBalance(BigDecimal.ZERO);
        budget1.setTotalExpense(BigDecimal.ZERO);
        budget1.setTotalIncome(BigDecimal.ZERO);
        Budget savedBudget = budgetRepository.save(budget1);

        Long nonExistentIncomeId = 999L;
        Long budgetId = savedBudget.getId();

        mockMvc.perform(patch("/api/budgets/" + budgetId + "/incomes/" + nonExistentIncomeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateIncomeJson))
                .andExpect(status().isConflict());
    }

    @Test
    public void updateIncome_Unauthorized_ShouldThrowException() throws Exception {
        String updateIncomeJson = "{\"amount\": \"200.0\", \"name\": \"Updated Income\"}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = new Budget();
        budget1.setName("Test Budget 1");
        budget1.setUser(user);
        budget1.setBalance(BigDecimal.ZERO);
        budget1.setTotalExpense(BigDecimal.ZERO);
        budget1.setTotalIncome(BigDecimal.ZERO);
        Budget savedBudget = budgetRepository.save(budget1);

        Income income = new Income();
        income.setName("Test Income");
        income.setAmount(BigDecimal.valueOf(100.0));
        income.setBudget(savedBudget);
        income.setUser(user);
        Income savedIncome = incomeRepository.save(income);
        Long incomeId = savedIncome.getId();
        Long budgetId = savedBudget.getId();

        SecurityContextHolder.clearContext();

        mockMvc.perform(patch("/api/budgets/" + budgetId + "/incomes/" + incomeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateIncomeJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getTotalExpensesForBudgetAndCategory_Success() throws Exception, InvalidInputException {
        String expectedTotalExpensesJson = "{\"budgetId\":1, \"categoryId\":1, \"value\":150.0}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = new Budget();
        budget1.setId(1L);
        budget1.setName("Test Budget 1");
        budget1.setUser(user);
        budget1.setBalance(BigDecimal.valueOf(0));
        budget1.setTotalExpense(BigDecimal.valueOf(0));
        budget1.setTotalIncome(BigDecimal.valueOf(0));
        Budget savedBudget = budgetRepository.save(budget1);


        Expense expense1 = new Expense();
        expense1.setName("Test Expense");
        expense1.setAmount(BigDecimal.valueOf(100.0));
        expense1.setIdCategory(1L);

        Expense expense2 = new Expense();
        expense2.setName("Test Expense 1");
        expense2.setAmount(BigDecimal.valueOf(50.0));
        expense2.setIdCategory(1L);

        budgetService.createExpense(budget1.getId(), expense1, user );
        budgetService.createExpense(budget1.getId(),expense2,user );

        Long budgetId = savedBudget.getId();
        Long categoryId = 1L;

        mockMvc.perform(get("/api/budgets/" + budgetId + "/categories/" + categoryId + "/expenses/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedTotalExpensesJson));
    }

    @Test
    public void getTotalExpensesForBudgetAndSubcategory_Success() throws Exception {
        User user = userRepository.findById(1L).get();

        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setUser(user);
        budget.setBalance(BigDecimal.ZERO);
        budget.setTotalExpense(BigDecimal.ZERO);
        budget.setTotalIncome(BigDecimal.ZERO);
        Budget savedBudget = budgetRepository.save(budget);

        Category category = new Category();
        category.setName("Test Category");
        category.setUser(user);
        Category savedCategory = categoryRepository.save(category);

        SubCategory subCategory = new SubCategory();
        subCategory.setName("Test SubCategory");
        subCategory.setCategory(savedCategory);
        SubCategory savedSubCategory = subCategoryRepository.save(subCategory);

        Expense expense1 = new Expense();
        expense1.setName("Test Expense 1");
        expense1.setAmount(BigDecimal.valueOf(100.0));
        expense1.setBudget(savedBudget);
        expense1.setIdCategory(savedCategory.getId());
        expense1.setIdSubCategory(savedSubCategory.getId());
        expense1.setUser(user);
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setName("Test Expense 2");
        expense2.setAmount(BigDecimal.valueOf(50.0));
        expense2.setBudget(savedBudget);
        expense2.setIdCategory(savedCategory.getId());
        expense2.setIdSubCategory(savedSubCategory.getId());
        expense2.setUser(user);
        expenseRepository.save(expense2);

        String expectedTotalExpensesJson = "{\"value\":150.0,\"subcategoryId\":" + savedSubCategory.getId() + ",\"typeAggregate\":\"EXPENSES_BY_SUBCATEGORY\",\"budgetId\":" + savedBudget.getId() + ",\"userId\":" + user.getId() + "}";

        mockMvc.perform(get("/api/budgets/" + savedBudget.getId() + "/subcategories/" + savedSubCategory.getId() + "/expenses/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedTotalExpensesJson));
    }

    @Test
    public void getTotalIncomesForBudgetAndCategory_Success() throws Exception, InvalidInputException {
        User user = userRepository.findById(1L).get();

        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setUser(user);
        budget.setBalance(BigDecimal.valueOf(0));
        budget.setTotalExpense(BigDecimal.valueOf(0));
        budget.setTotalIncome(BigDecimal.valueOf(0));
        Budget savedBudget = budgetRepository.save(budget);

        Category category = new Category();
        category.setName("Test Category");
        category.setUser(user);
        Category savedCategory = categoryRepository.save(category);

        Income income1 = new Income();
        income1.setName("Test Income");
        income1.setAmount(BigDecimal.valueOf(500.0));
        income1.setIdCategory(savedCategory.getId());

        Income income2 = new Income();
        income2.setName("Test Income 1");
        income2.setAmount(BigDecimal.valueOf(250.0));
        income2.setIdCategory(savedCategory.getId());

        budgetService.createIncome(budget.getId(), income1, user);
        budgetService.createIncome(budget.getId(), income2, user);

        mockMvc.perform(get("/api/budgets/" + savedBudget.getId() + "/categories/" + savedCategory.getId() + "/incomes/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(750.0))
                .andExpect(jsonPath("$.categoryId").value(savedCategory.getId()))
                .andExpect(jsonPath("$.budgetId").value(savedBudget.getId()));
    }

    @Test
    public void getTotalIncomesForBudgetAndSubcategory_Success() throws Exception, InvalidInputException {
        User user = userRepository.findById(1L).get();

        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setUser(user);
        budget.setBalance(BigDecimal.ZERO);
        budget.setTotalExpense(BigDecimal.ZERO);
        budget.setTotalIncome(BigDecimal.ZERO);
        Budget savedBudget = budgetRepository.save(budget);

        Category category = new Category();
        category.setName("Test Category");
        category.setUser(user);
        Category savedCategory = categoryRepository.save(category);

        SubCategory subCategory = new SubCategory();
        subCategory.setName("Test SubCategory");
        subCategory.setCategory(savedCategory);
        SubCategory savedSubCategory = subCategoryRepository.save(subCategory);

        Income income = new Income();
        income.setName("Test Income");
        income.setAmount(BigDecimal.valueOf(500.0));
        income.setBudget(savedBudget);
        income.setIdCategory(savedCategory.getId());
        income.setIdSubCategory(savedSubCategory.getId());
        income.setUser(user);
        incomeRepository.save(income);

        Income income1 = new Income();
        income1.setName("Test Income 1");
        income1.setAmount(BigDecimal.valueOf(250.0));
        income1.setBudget(savedBudget);
        income1.setIdCategory(savedCategory.getId());
        income1.setIdSubCategory(savedSubCategory.getId());
        income1.setUser(user);
        incomeRepository.save(income1);

        mockMvc.perform(get("/api/budgets/" + savedBudget.getId() + "/subcategories/" + savedSubCategory.getId() + "/incomes/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(750.0))
                .andExpect(jsonPath("$.subcategoryId").value(savedSubCategory.getId()))
                .andExpect(jsonPath("$.budgetId").value(savedBudget.getId()));
    }

    @Test
    public void deleteBudget_Success() throws Exception {
        User user = userRepository.findById(1L).get();

        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setUser(user);
        Budget savedBudget = budgetRepository.save(budget);

        mockMvc.perform(delete("/api/budgets/" + savedBudget.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM budget", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void deleteBudget_NotFound() throws Exception {
        Long nonExistentBudgetId = 999L;

        mockMvc.perform(delete("/api/budgets/" + nonExistentBudgetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void deleteBudget_Unauthorized() throws Exception {
        Long budgetId = 1L;
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/budgets/" + budgetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteExpense_Success() throws Exception {
        User user = userRepository.findById(1L).get();

        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setUser(user);
        Budget savedBudget = budgetRepository.save(budget);

        Expense expense = new Expense();
        expense.setName("Test Expense");
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setBudget(savedBudget);
        expense.setUser(user);
        Expense savedExpense = expenseRepository.save(expense);

        mockMvc.perform(delete("/api/budgets/" + savedBudget.getId() + "/expenses/" + savedExpense.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expense", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void deleteExpense_NotFound() throws Exception {
        Long nonExistentExpenseId = 999L;
        Long budgetId = 1L;

        mockMvc.perform(delete("/api/budgets/" + budgetId + "/expenses/" + nonExistentExpenseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void deleteExpense_Unauthorized() throws Exception {
        Long budgetId = 1L;
        Long expenseId = 1L;
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/budgets/" + budgetId + "/expenses/" + expenseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteIncome_Success() throws Exception {
        User user = userRepository.findById(1L).get();

        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setUser(user);
        Budget savedBudget = budgetRepository.save(budget);

        Income income = new Income();
        income.setName("Test Income");
        income.setAmount(BigDecimal.valueOf(100));
        income.setBudget(savedBudget);
        income.setUser(user);
        Income savedIncome = incomeRepository.save(income);

        mockMvc.perform(delete("/api/budgets/" + savedBudget.getId() + "/incomes/" + savedIncome.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM income", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void deleteIncome_NotFound() throws Exception {
        Long nonExistentIncomeId = 999L;
        Long budgetId = 1L;

        mockMvc.perform(delete("/api/budgets/" + budgetId + "/incomes/" + nonExistentIncomeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void deleteIncome_Unauthorized() throws Exception {
        Long budgetId = 1L;
        Long incomeId = 1L;
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/budgets/" + budgetId + "/incomes/" + incomeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void calculateExpensesSummary_Success() throws Exception {
        User user = userRepository.findById(1L).get();

        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setUser(user);
        Budget savedBudget = budgetRepository.save(budget);

        Expense expense1 = new Expense();
        expense1.setName("Test Expense 1");
        expense1.setAmount(BigDecimal.valueOf(50));
        expense1.setBudget(savedBudget);
        expense1.setUser(user);
        expenseRepository.save(expense1);

        Expense expense2 = new Expense();
        expense2.setName("Test Expense 2");
        expense2.setAmount(BigDecimal.valueOf(100));
        expense2.setBudget(savedBudget);
        expense2.setUser(user);
        expenseRepository.save(expense2);

        mockMvc.perform(get("/api/budgets/" + savedBudget.getId() + "/expenses/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.expensesSummary[0].totalExpenses").value(150));

    }

    @Test
    public void calculateExpensesSummary_Unauthorized() throws Exception {
        Long budgetId = 1L;
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/budgets/" + budgetId + "/expenses/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void uploadCsv_Success_MILLENIUM() throws Exception {
        Optional<User> optionalUser = userRepository.findById(1L);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("User not found");
        }
        User user = optionalUser.get();

        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setUser(user);
        Budget savedBudget = budgetRepository.save(budget);

        String csvContent = "header\nPL11 1111 1111 1111 1111 1111 1111,2024-05-09,2024-05-09,ZAKUP - FIZ. UŻYCIE KARTY,,,TEST,-9.07,,,PLN\nPL11 1111 1111 1111 1111 1111 1111,2024-05-08,2024-05-08,PRZELEW PRZYCHODZĄCY,11 11 1111 1111 1111 1111 1111 11,TEST USER,TEST TITLE,,1000,,PLN";
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/api/budgets/" + savedBudget.getId() + "/upload-csv/MILLENIUM")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully uploaded 'test.csv' for MILLENIUM"));

        List<Expense> expenses = expenseRepository.findAll();
        Assertions.assertFalse(expenses.isEmpty());
        Assertions.assertEquals(1, expenses.size());

        Expense expense = expenses.get(0);
        Assertions.assertEquals("TEST", expense.getName());
        Assertions.assertEquals(BigDecimal.valueOf(-9.07), expense.getAmount());
        Assertions.assertEquals(savedBudget.getId(), expense.getBudget().getId());
        Assertions.assertEquals(user.getId(), expense.getUser().getId());

        List<Income> incomes = incomeRepository.findAll();
        Assertions.assertFalse(incomes.isEmpty());
        Assertions.assertEquals(1, incomes.size());

        Income income = incomes.get(0);
        Assertions.assertEquals("TEST TITLE", income.getName());
        Assertions.assertEquals(BigDecimal.valueOf(1000), income.getAmount());
        Assertions.assertEquals(savedBudget.getId(), income.getBudget().getId());
        Assertions.assertEquals(user.getId(), income.getUser().getId());

    }

    @Test
    public void uploadCsv_Success_SANTANDER() throws Exception {
        Optional<User> optionalUser = userRepository.findById(1L);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("User not found");
        }
        User user = optionalUser.get();

        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setUser(user);
        Budget savedBudget = budgetRepository.save(budget);

        String csvContent = "header\n2023/005;19-08-2023;17-08-2023;TEST TITLE EXPENSE;;;;;TRANSAKCJA KARTĄ;;71,15;;114,16;12;T\n2023/005;11-08-2023;11-08-2023;TEST TITLE INCOME;;;;;WPŁATA GOTÓWKI - WPŁATOMAT;;;200,00;185,31;11;T";
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/api/budgets/" + savedBudget.getId() + "/upload-csv/SANTANDER")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully uploaded 'test.csv' for SANTANDER"));

        List<Expense> expenses = expenseRepository.findAll();
        Assertions.assertFalse(expenses.isEmpty());
        Assertions.assertEquals(1, expenses.size());

        Expense expense = expenses.get(0);
        Assertions.assertEquals("TEST TITLE EXPENSE", expense.getName());
        Assertions.assertEquals(BigDecimal.valueOf(71.15), expense.getAmount());
        Assertions.assertEquals(savedBudget.getId(), expense.getBudget().getId());
        Assertions.assertEquals(user.getId(), expense.getUser().getId());

        List<Income> incomes = incomeRepository.findAll();
        Assertions.assertFalse(incomes.isEmpty());
        Assertions.assertEquals(1, incomes.size());

        Income income = incomes.get(0);
        Assertions.assertEquals("TEST TITLE INCOME", income.getName());
        Assertions.assertEquals(0, income.getAmount().compareTo(BigDecimal.valueOf(200.00)));
        Assertions.assertEquals(savedBudget.getId(), income.getBudget().getId());
        Assertions.assertEquals(user.getId(), income.getUser().getId());
    }

    @Test
    public void uploadCsv_EmptyFile() throws Exception {
        Optional<User> optionalUser = userRepository.findById(1L);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("User not found");
        }
        User user = optionalUser.get();

        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setUser(user);
        Budget savedBudget = budgetRepository.save(budget);

        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv", "".getBytes());

        mockMvc.perform(multipart("/api/budgets/" + savedBudget.getId() + "/upload-csv/MILLENIUM")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File is empty"));

        List<Expense> expenses = expenseRepository.findAll();
        Assertions.assertTrue(expenses.isEmpty());

        List<Income> incomes = incomeRepository.findAll();
        Assertions.assertTrue(incomes.isEmpty());
    }

    @Test
    public void uploadCsv_UnsupportedBank() throws Exception {
        Optional<User> optionalUser = userRepository.findById(1L);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("User not found");
        }
        User user = optionalUser.get();

        Budget budget = new Budget();
        budget.setName("Test Budget");
        budget.setUser(user);
        Budget savedBudget = budgetRepository.save(budget);

        String csvContent = "header\nsome,data,for,unsupported,bank";
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/api/budgets/" + savedBudget.getId() + "/upload-csv/UNSUPPORTED_BANK")
                        .file(mockFile)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        List<Expense> expenses = expenseRepository.findAll();
        Assertions.assertTrue(expenses.isEmpty());

        List<Income> incomes = incomeRepository.findAll();
        Assertions.assertTrue(incomes.isEmpty());
    }


}
package com.myfund.integration;

import com.myfund.exceptions.InvalidInputException;
import com.myfund.models.*;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    public void createBudget_Success() throws Exception {

        String createBudgetJson = "{\"name\":\"Test Budget\"}";

        mockMvc.perform(post("/api/v1/budgets")
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

        mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBudgetJson))
                .andExpect(status().isBadRequest());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM budget", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void createBudget_InvalidJson() throws Exception {
        String createBudgetJson = "{\"invalidField\":\"value\"}";

        mockMvc.perform(post("/api/v1/budgets")
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

        mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBudgetJson))
                .andExpect(status().isUnauthorized());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM budget", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void createBudget_NameAlreadyExists_ShouldThrowException() throws Exception {
        String createBudgetJson = "{\"name\":\"Test Budget\"}";

        Budget existingBudget = Budget.builder().user(userRepository.findById(1L).get()).name("Test Budget").build();

        budgetRepository.save(existingBudget);

        mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBudgetJson))
                .andExpect(status().isConflict());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM budget", Integer.class);
        Assertions.assertEquals(1, count);
    }

    @Test
    public void getBudgetById_Success() throws Exception {
        String expectedBudgetJson = "{\"id\":1, \"name\":\"Test Budget\"}";

        Budget budget = Budget.builder().user(userRepository.findById(1L).get()).id(1L).name("Test Budget").build();

        budgetRepository.save(budget);

        mockMvc.perform(get("/api/v1/budgets/" + budget.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedBudgetJson));
    }

    @Test
    public void getBudgetById_NotFound() throws Exception {
        Long nonExistentBudgetId = 999L;

        mockMvc.perform(get("/api/v1/budgets/" + nonExistentBudgetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void getBudgetById_Unauthorized() throws Exception {
        Long budgetId = 1L;
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/budgets/" + budgetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAllBudgets_Success() throws Exception {
        String expectedBudgetsJson = "[{\"id\":1, \"name\":\"Test Budget 1\"}, {\"id\":2, \"name\":\"Test Budget 2\"}]"; // Replace with the expected JSON representation of the budgets
        User user = userRepository.findById(1L).get();

        Budget budget1 = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .build();

        Budget budget2 = Budget.builder()
                .name("Test Budget 2")
                .user(user)
                .build();

        budgetRepository.save(budget1);
        budgetRepository.save(budget2);

        mockMvc.perform(get("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedBudgetsJson));
    }

    @Test
    public void getAllBudgets_Unauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void createExpense_Success() throws Exception {
        String createExpenseJson = "{\"amount\": \"100.0\", \"name\": \"Test Expense\"}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget1);
        Long budgetId = savedBudget.getId();
        mockMvc.perform(post("/api/v1/budgets/" + budgetId + "/expenses", budgetId)
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

        Budget budget1 = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .build();

        Budget savedBudget = budgetRepository.save(budget1);
        Long budgetId = savedBudget.getId();
        mockMvc.perform(post("/api/v1/budgets/" + budgetId + "/expenses", budgetId)
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

        Budget budget1 = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget1);
        Long budgetId = savedBudget.getId();
        mockMvc.perform(post("/api/v1/budgets/" + budgetId + "/expenses", budgetId)
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

        Budget budget1 = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget1);
        Long budgetId = savedBudget.getId();
        mockMvc.perform(post("/api/v1/budgets/" + budgetId + "/expenses", budgetId)
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

        Budget budget1 = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget1);
        Long budgetId = savedBudget.getId();
        SecurityContextHolder.clearContext();
        mockMvc.perform(post("/api/v1/budgets/" + budgetId + "/expenses", budgetId)
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

        mockMvc.perform(post("/api/v1/budgets/" + nonExistentBudgetId + "/expenses", nonExistentBudgetId)
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
        Budget budget = Budget.builder()
                .name("Test Budget 1")
                .user(userRepository.findById(1L).get())
                .build();

        budgetRepository.save(budget);

        mockMvc.perform(post("/api/v1/budgets/" + budgetId + "/incomes")
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
        User user = userRepository.findById(1L).get();

        Budget budget1 = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .build();
        budgetRepository.save(budget1);

        mockMvc.perform(post("/api/v1/budgets/" + budgetId + "/incomes")
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

        mockMvc.perform(post("/api/v1/budgets/" + nonExistentBudgetId + "/incomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createIncomeJson))
                .andExpect(status().isConflict());
    }

    @Test
    public void createIncome_Unauthorized_ShouldReturnUnauthorized() throws Exception {
        String createIncomeJson = "{\"name\":\"Test Income\", \"amount\": 100.0, \"categoryId\": 1, \"subcategoryId\": 1}";
        SecurityContextHolder.clearContext();
        User user = userRepository.findById(1L).get();

        Budget budget1 = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget1);
        Long budgetId = savedBudget.getId();

        mockMvc.perform(post("/api/v1/budgets/" + budgetId + "/incomes")
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

        Budget budget = Budget.builder()
                .user(user)
                .id(budgetId)
                .name("Test Budget")
                .build();
        budgetRepository.save(budget);

        Expense expense = Expense.builder()
                .user(user)
                .budget(budget)
                .id(expenseId)
                .name("Test Expense")
                .amount(BigDecimal.valueOf(100.0))
                .build();
        expenseRepository.save(expense);

        mockMvc.perform(patch("/api/v1/budgets/" + budgetId + "/expenses/" + expenseId)
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

        Budget budget = Budget.builder()
                .user(user)
                .id(budgetId)
                .name("Test Budget")
                .build();
        budgetRepository.save(budget);

        Expense expense = Expense.builder()
                .user(user)
                .budget(budget)
                .id(expenseId)
                .name("Test Expense")
                .amount(BigDecimal.valueOf(100.0))
                .build();
        expenseRepository.save(expense);

        mockMvc.perform(patch("/api/v1/budgets/" + budgetId + "/expenses/" + expenseId)
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

        Budget budget = Budget.builder()
                .user(user)
                .id(budgetId)
                .name("Test Budget")
                .build();
        budgetRepository.save(budget);

        Expense expense = Expense.builder()
                .user(user)
                .budget(budget)
                .id(expenseId)
                .name("Test Expense")
                .amount(BigDecimal.valueOf(100.0))
                .build();
        expenseRepository.save(expense);


        mockMvc.perform(patch("/api/v1/budgets/" + budgetId + "/expenses/" + nonExistentExpenseId)
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

        mockMvc.perform(patch("/api/v1/budgets/" + budgetId + "/expenses/" + expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpenseJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateIncome_Success() throws Exception {
        String updateIncomeJson = "{\"amount\": \"200.0\", \"name\": \"Updated Income\"}";

        User user = userRepository.findById(1L).get();

        Long budgetId = 1L;
        Long incomeId = 1L;

        Budget budget1 = Budget.builder()
                .user(user)
                .id(budgetId)
                .name("Test Budget")
                .build();
        budgetRepository.save(budget1);

        Income income = Income.builder()
                .id(incomeId)
                .budget(budget1)
                .name("Test Income")
                .amount(BigDecimal.valueOf(100.0))
                .localDateTime(LocalDate.now().atStartOfDay())
                .user(user)
                .build();
        incomeRepository.save(income);

        mockMvc.perform(patch("/api/v1/budgets/" + budgetId + "/incomes/" + incomeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateIncomeJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"id\":1, \"name\":\"Updated Income\", \"amount\":200.0}"));

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

        Budget budget1 = Budget.builder()
                .user(user)
                .id(1L)
                .name("Test Budget")

                .build();
        Budget savedBudget = budgetRepository.save(budget1);

        Income income = Income.builder()
                .name("Test Income")
                .amount(BigDecimal.valueOf(100.0))
                .budget(savedBudget)
                .user(user)
                .build();
        Income savedIncome = incomeRepository.save(income);
        Long incomeId = savedIncome.getId();
        Long budgetId = savedBudget.getId();

        mockMvc.perform(patch("/api/v1/budgets/" + budgetId + "/incomes/" + incomeId)
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

        Budget budget1 = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget1);

        Income income = Income.builder()
                .name("Test Income")
                .amount(BigDecimal.valueOf(100.0))
                .budget(savedBudget)
                .user(user)
                .build();
        Income savedIncome = incomeRepository.save(income);
        Long incomeId = savedIncome.getId();
        Long budgetId = savedBudget.getId();

        mockMvc.perform(patch("/api/v1/budgets/" + budgetId + "/incomes/" + incomeId)
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

        Budget budget1 = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget1);

        Long nonExistentIncomeId = 999L;
        Long budgetId = savedBudget.getId();

        mockMvc.perform(patch("/api/v1/budgets/" + budgetId + "/incomes/" + nonExistentIncomeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateIncomeJson))
                .andExpect(status().isConflict());
    }

    @Test
    public void updateIncome_Unauthorized_ShouldThrowException() throws Exception {
        String updateIncomeJson = "{\"amount\": \"200.0\", \"name\": \"Updated Income\"}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget1);

        Income income = Income.builder()
                .name("Test Income")
                .amount(BigDecimal.valueOf(100.0))
                .budget(savedBudget)
                .user(user)
                .build();
        Income savedIncome = incomeRepository.save(income);
        Long incomeId = savedIncome.getId();
        Long budgetId = savedBudget.getId();

        SecurityContextHolder.clearContext();

        mockMvc.perform(patch("/api/v1/budgets/" + budgetId + "/incomes/" + incomeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateIncomeJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getTotalExpensesForBudgetAndCategory_Success() throws Exception, InvalidInputException {
        String expectedTotalExpensesJson = "{\"budgetId\":1, \"categoryId\":1, \"value\":150.0}";

        User user = userRepository.findById(1L).get();

        Budget budget1 = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .id(1L)
                .build();
        Budget savedBudget = budgetRepository.save(budget1);


        Expense expense1 = Expense.builder()
                .name("Test Expense")
                .amount(BigDecimal.valueOf(100.0))
                .idCategory(1L)
                .build();

        Expense expense2 = Expense.builder()
                .name("Test Expense 1")
                .amount(BigDecimal.valueOf(50.0))
                .idCategory(1L)
                .build();

        budgetService.createExpense(budget1.getId(), expense1, user );
        budgetService.createExpense(budget1.getId(),expense2,user );

        Long budgetId = savedBudget.getId();
        Long categoryId = 1L;

        mockMvc.perform(get("/api/v1/budgets/" + budgetId + "/categories/" + categoryId + "/expenses/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedTotalExpensesJson));
    }

    @Test
    public void getTotalExpensesForBudgetAndSubcategory_Success() throws Exception {
        User user = userRepository.findById(1L).get();

        Budget budget = Budget.builder()
                .name("Test Budget 1")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        Category category = Category.builder()
                .name("Test Category")
                .user(user)
                .build();;
        Category savedCategory = categoryRepository.save(category);

        SubCategory subCategory = SubCategory.builder()
                .name("Test SubCategory")
                .category(savedCategory)
                .build();
        SubCategory savedSubCategory = subCategoryRepository.save(subCategory);

        Expense expense1 = Expense.builder()
                .name("Test Expense 1")
                .amount(BigDecimal.valueOf(100.0))
                .budget(savedBudget)
                .idCategory(savedCategory.getId())
                .idSubCategory(savedSubCategory.getId())
                .user(user)
                .build();
        expenseRepository.save(expense1);

        Expense expense2 = Expense.builder()
                .name("Test Expense 2")
                .amount(BigDecimal.valueOf(50.0))
                .budget(savedBudget)
                .idCategory(savedCategory.getId())
                .idSubCategory(savedSubCategory.getId())
                .user(user)
                .build();
        expenseRepository.save(expense2);

        String expectedTotalExpensesJson = "{\"value\":150.0,\"subcategoryId\":" + savedSubCategory.getId() + ",\"typeAggregate\":\"EXPENSES_BY_SUBCATEGORY\",\"budgetId\":" + savedBudget.getId() + ",\"userId\":" + user.getId() + "}";

        mockMvc.perform(get("/api/v1/budgets/" + savedBudget.getId() + "/subcategories/" + savedSubCategory.getId() + "/expenses/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedTotalExpensesJson));
    }

    @Test
    public void getTotalIncomesForBudgetAndCategory_Success() throws Exception, InvalidInputException {
        User user = userRepository.findById(1L).get();

        Budget budget = Budget.builder()
                .name("Test Budget")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        Category category = Category.builder()
                .name("Test Category")
                .user(user)
                .build();
        Category savedCategory = categoryRepository.save(category);

        Income income1 = Income.builder()
                .name("Test Income")
                .amount(BigDecimal.valueOf(500.0))
                .idCategory(savedCategory.getId())
                .budget(savedBudget)
                .user(user)
                .build();

        Income income2 = Income.builder()
                .name("Test Income 1")
                .amount(BigDecimal.valueOf(250.0))
                .idCategory(savedCategory.getId())
                .budget(savedBudget)
                .user(user)
                .build();

        budgetService.createIncome(budget.getId(), income1, user);
        budgetService.createIncome(budget.getId(), income2, user);

        mockMvc.perform(get("/api/v1/budgets/" + savedBudget.getId() + "/categories/" + savedCategory.getId() + "/incomes/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(750.0))
                .andExpect(jsonPath("$.categoryId").value(savedCategory.getId()))
                .andExpect(jsonPath("$.budgetId").value(savedBudget.getId()));
    }

    @Test
    public void getTotalIncomesForBudgetAndSubcategory_Success() throws Exception, InvalidInputException {
        User user = userRepository.findById(1L).get();

        Budget budget = Budget.builder()
                .name("Test Budget")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        Category category = Category.builder()
                .name("Test Category")
                .user(user)
                .build();
        Category savedCategory = categoryRepository.save(category);
        SubCategory subCategory = SubCategory.builder()
                .name("Test SubCategory")
                .category(savedCategory)
                .build();
        SubCategory savedSubCategory = subCategoryRepository.save(subCategory);

        Income income = Income.builder()
                .name("Test Income")
                .amount(BigDecimal.valueOf(500.0))
                .budget(savedBudget)
                .idCategory(savedCategory.getId())
                .idSubCategory(savedSubCategory.getId())
                .user(user)
                .build();
        incomeRepository.save(income);

        Income income1 = Income.builder()
                .name("Test Income 1")
                .amount(BigDecimal.valueOf(250.0))
                .budget(savedBudget)
                .idCategory(savedCategory.getId())
                .idSubCategory(savedSubCategory.getId())
                .user(user)
                .build();
        incomeRepository.save(income1);
        mockMvc.perform(get("/api/v1/budgets/" + savedBudget.getId() + "/subcategories/" + savedSubCategory.getId() + "/incomes/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(750.0))
                .andExpect(jsonPath("$.subcategoryId").value(savedSubCategory.getId()))
                .andExpect(jsonPath("$.budgetId").value(savedBudget.getId()));
    }

    @Test
    public void deleteBudget_Success() throws Exception {
        User user = userRepository.findById(1L).get();

        Budget budget = Budget.builder()
                .name("Test Budget")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        mockMvc.perform(delete("/api/v1/budgets/" + savedBudget.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM budget", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void deleteBudget_NotFound() throws Exception {
        Long nonExistentBudgetId = 999L;

        mockMvc.perform(delete("/api/v1/budgets/" + nonExistentBudgetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void deleteBudget_Unauthorized() throws Exception {
        Long budgetId = 1L;
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/v1/budgets/" + budgetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteExpense_Success() throws Exception {
        User user = userRepository.findById(1L).get();

        Budget budget = Budget.builder()
                .name("Test Budget")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        Expense expense = Expense.builder()
                .name("Test Expense")
                .amount(BigDecimal.valueOf(100))
                .budget(savedBudget)
                .user(user)
                .build();
        Expense savedExpense = expenseRepository.save(expense);

        mockMvc.perform(delete("/api/v1/budgets/" + savedBudget.getId() + "/expenses/" + savedExpense.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expense", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void deleteExpense_NotFound() throws Exception {
        Long nonExistentExpenseId = 999L;
        Long budgetId = 1L;

        mockMvc.perform(delete("/api/v1/budgets/" + budgetId + "/expenses/" + nonExistentExpenseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void deleteExpense_Unauthorized() throws Exception {
        Long budgetId = 1L;
        Long expenseId = 1L;
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/v1/budgets/" + budgetId + "/expenses/" + expenseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteIncome_Success() throws Exception {
        User user = userRepository.findById(1L).get();

        Budget budget = Budget.builder()
                .name("Test Budget")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        Income income = Income.builder()
                .name("Test Income")
                .amount(BigDecimal.valueOf(100))
                .budget(savedBudget)
                .user(user)
                .build();
        Income savedIncome = incomeRepository.save(income);

        mockMvc.perform(delete("/api/v1/budgets/" + savedBudget.getId() + "/incomes/" + savedIncome.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM income", Integer.class);
        Assertions.assertEquals(0, count);
    }

    @Test
    public void deleteIncome_NotFound() throws Exception {
        Long nonExistentIncomeId = 999L;
        Long budgetId = 1L;

        mockMvc.perform(delete("/api/v1/budgets/" + budgetId + "/incomes/" + nonExistentIncomeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void deleteIncome_Unauthorized() throws Exception {
        Long budgetId = 1L;
        Long incomeId = 1L;
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/v1/budgets/" + budgetId + "/incomes/" + incomeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void calculateExpensesSummary_Success() throws Exception {
        User user = userRepository.findById(1L).get();

        Budget budget = Budget.builder()
                .name("Test Budget")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        Expense expense1 = Expense.builder()
                .name("Test Expense 1")
                .amount(BigDecimal.valueOf(50))
                .budget(savedBudget)
                .user(user)
                .build();
        expenseRepository.save(expense1);

        Expense expense2 = Expense.builder()
                .name("Test Expense 2")
                .amount(BigDecimal.valueOf(100))
                .budget(savedBudget)
                .user(user)
                .build();
        expenseRepository.save(expense2);

        mockMvc.perform(get("/api/v1/budgets/" + savedBudget.getId() + "/expenses/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.expensesSummary[0].totalExpenses").value(150));

    }

    @Test
    public void calculateExpensesSummary_Unauthorized() throws Exception {
        Long budgetId = 1L;
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/budgets/" + budgetId + "/expenses/summary")
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

        Budget budget = Budget.builder()
                .name("Test Budget")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        String csvContent = "header\nPL11 1111 1111 1111 1111 1111 1111,2024-05-09,2024-05-09,ZAKUP - FIZ. UŻYCIE KARTY,,,TEST,-9.07,,,PLN\nPL11 1111 1111 1111 1111 1111 1111,2024-05-08,2024-05-08,PRZELEW PRZYCHODZĄCY,11 11 1111 1111 1111 1111 1111 11,TEST USER,TEST TITLE,,1000,,PLN";
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/api/v1/budgets/" + savedBudget.getId() + "/upload-csv/MILLENIUM")
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

        Budget budget = Budget.builder()
                .name("Test Budget")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        String csvContent = "header\n2023/005;19-08-2023;17-08-2023;TEST TITLE EXPENSE;;;;;TRANSAKCJA KARTĄ;;71,15;;114,16;12;T\n2023/005;11-08-2023;11-08-2023;TEST TITLE INCOME;;;;;WPŁATA GOTÓWKI - WPŁATOMAT;;;200,00;185,31;11;T";
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/api/v1/budgets/" + savedBudget.getId() + "/upload-csv/SANTANDER")
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

        Budget budget = Budget.builder()
                .name("Test Budget")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv", "".getBytes());

        mockMvc.perform(multipart("/api/v1/budgets/" + savedBudget.getId() + "/upload-csv/MILLENIUM")
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

        Budget budget = Budget.builder()
                .name("Test Budget")
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        String csvContent = "header\nsome,data,for,unsupported,bank";
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/api/v1/budgets/" + savedBudget.getId() + "/upload-csv/UNSUPPORTED_BANK")
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
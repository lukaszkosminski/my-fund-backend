package com.myfund.models;

import com.myfund.services.encryption.BigDecimalEncryptor;
import com.myfund.services.encryption.LocalDateTimeEncryptor;
import com.myfund.services.encryption.StringEncryptor;
import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(name = "expense")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Convert(converter = StringEncryptor.class)
    private String name;

    @NotNull
    @Convert(converter = BigDecimalEncryptor.class)
    private BigDecimal amount;

    @Convert(converter = LocalDateTimeEncryptor.class)
    private LocalDateTime localDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private Budget budget;

    @Column(name = "category_id")
    private Long idCategory;

    @Column(name = "sub_category_id")
    private Long idSubCategory;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public static Expense create(Budget budget, User user, Expense expense) {
        return Expense.builder()
                .name(expense.getName())
                .idCategory(expense.getIdCategory())
                .idSubCategory(expense.getIdSubCategory())
                .amount(expense.getAmount())
                .localDateTime(LocalDate.now().atStartOfDay())
                .budget(budget)
                .user(user)
                .build();
    }

    public static Expense update(Expense expense, Expense newExpense) {
        return Expense.builder()
                .id(expense.getId())
                .name(newExpense.getName())
                .idCategory(newExpense.getIdCategory())
                .idSubCategory(newExpense.getIdSubCategory())
                .amount(newExpense.getAmount())
                .localDateTime(LocalDate.now().atStartOfDay())
                .budget(expense.getBudget())
                .user(expense.getUser())
                .build();
    }
}
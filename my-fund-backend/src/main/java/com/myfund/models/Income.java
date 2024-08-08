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

@Table(name = "income")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Income {

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

    public static Income create(Budget budget, User user, Income income) {
        return Income.builder()
                .name(income.getName())
                .idCategory(income.getIdCategory())
                .idSubCategory(income.getIdSubCategory())
                .amount(income.getAmount())
                .localDateTime(LocalDate.now().atStartOfDay())
                .budget(budget)
                .user(user)
                .build();
    }

    public static Income update(Income income, Income newIncome) {
        return Income.builder()
                .id(income.getId())
                .name(newIncome.getName())
                .idCategory(newIncome.getIdCategory())
                .idSubCategory(newIncome.getIdSubCategory())
                .amount(newIncome.getAmount())
                .localDateTime(LocalDate.now().atStartOfDay())
                .budget(income.getBudget())
                .user(income.getUser())
                .build();
    }

}
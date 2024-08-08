package com.myfund.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.myfund.services.encryption.StringEncryptor;
import com.myfund.services.encryption.LocalDateTimeEncryptor;
import com.myfund.services.encryption.BigDecimalEncryptor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "budget")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = StringEncryptor.class)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Convert(converter = LocalDateTimeEncryptor.class)
    private LocalDateTime localDateTime;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Expense> expenses;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Income> incomes;

    @Convert(converter = BigDecimalEncryptor.class)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Convert(converter = BigDecimalEncryptor.class)
    @Builder.Default
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Convert(converter = BigDecimalEncryptor.class)
    @Builder.Default
    private BigDecimal totalExpense = BigDecimal.ZERO;

    public static Budget createDefault(User user) {
        return Budget.builder()
                .name("Default Budget")
                .user(user)
                .localDateTime(LocalDateTime.now())
                .build();
    }

    public static Budget create(Budget budget, User user) {
        return Budget.builder()
                .name(budget.getName())
                .user(user)
                .localDateTime(LocalDateTime.now())
                .build();
    }

}

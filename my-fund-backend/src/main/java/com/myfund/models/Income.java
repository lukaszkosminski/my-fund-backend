package com.myfund.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Table(name = "income")
@Getter
@Setter
@Entity
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private BigDecimal amount;

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

}

package com.myfund.models;

import com.myfund.services.encryption.BigDecimalEncryptor;
import com.myfund.services.encryption.LocalDateTimeEncryptor;
import com.myfund.services.encryption.StringEncryptor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "income")
@Getter
@Setter
@Entity
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
    private LocalDate localDate;

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
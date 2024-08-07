package com.myfund.models;

import com.myfund.services.encryption.StringEncryptor;
import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Table(name = "category")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Convert(converter = StringEncryptor.class)
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true,  fetch = FetchType.LAZY)
    private List<SubCategory> subCategories;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}

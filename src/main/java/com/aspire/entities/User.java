package com.aspire.entities;

import com.aspire.enums.UserRoles;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private UserRoles role;

    @OneToMany(mappedBy = "user")
    private List<Loan> loans;
}

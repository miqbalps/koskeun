package com.example.koskeun.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "roles")
public class Role {
    public static final String ADMIN = "admin";
    public static final String PENCARI = "pencari";
    public static final String PEMILIK = "pemilik";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name; // admin, pencari, pemilik

    @OneToMany(mappedBy = "role")
    private List<User> users;

    public Role() {
        // Default constructor
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

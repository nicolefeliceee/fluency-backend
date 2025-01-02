package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 55)
    private String Name;
    private String email;
    private String phone;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    private String userType;
}

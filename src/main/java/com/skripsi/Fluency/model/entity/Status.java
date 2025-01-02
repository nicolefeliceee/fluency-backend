package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 55)
    private String label;
}

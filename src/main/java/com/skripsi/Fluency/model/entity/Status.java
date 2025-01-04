package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table
@Data
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 55)
    private String label;

//    tambahan
    @OneToMany(mappedBy = "status")
    private List<ProjectDetail> projectDetails;

    @OneToMany(mappedBy = "status")
    private List<ProjectHeader> projectHeaders;
}


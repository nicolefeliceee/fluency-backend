package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 55)
    private String label;

    private Boolean forBrand;
    private Boolean forInfluencer;

//    tambahan
    @OneToMany(mappedBy = "status")
    private List<ProjectDetail> projectDetails;

    @OneToMany(mappedBy = "status")
    private List<ProjectHeader> projectHeaders;
}


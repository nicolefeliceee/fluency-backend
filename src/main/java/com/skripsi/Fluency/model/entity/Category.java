package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 55)
    private String label;

//    tambahan
    @OneToMany(mappedBy = "category")
    private List<Brand> brands;

    //    many to many
    @ManyToMany(mappedBy = "categories")
    private List<Influencer> influencers;

}


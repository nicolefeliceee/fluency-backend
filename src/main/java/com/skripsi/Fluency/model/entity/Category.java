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
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 55)
    private String label;

    private String logo;
    private String activeLogo;

//    tambahan
    @OneToMany(mappedBy = "category")
    private List<Brand> brands;

    //    many to many
    @ManyToMany(mappedBy = "categories")
    private List<Influencer> influencers;

}


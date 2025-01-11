package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table
@Data
public class Gender {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 55)
    private String label;

    private String logo;

    //    tambahan
    @OneToMany(mappedBy = "gender")
    private List<Influencer> influencers;

    //    many to many
    @ManyToMany(mappedBy = "genders")
    private List<Brand> brands;

}

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
public class Gender {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 55)
    private String label;

    private String logo;
    private String activeLogo;

    //    tambahan
    @OneToMany(mappedBy = "gender")
    private List<Influencer> influencers;

    //    many to many
    @ManyToMany(mappedBy = "genders")
    private List<Brand> brands;

}

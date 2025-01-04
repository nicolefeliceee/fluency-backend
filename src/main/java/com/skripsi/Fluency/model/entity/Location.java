package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table
@Data
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 55)
    private String label;

    @OneToMany(mappedBy = "location")
    private List<User> users;

    //    many to many
    @ManyToMany(mappedBy = "locations")
    private List<Brand> brands;


}

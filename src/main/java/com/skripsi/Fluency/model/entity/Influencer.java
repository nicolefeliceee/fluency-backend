package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table
@Data
public class Influencer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "gender_id")
    private Gender gender;

    @Column
    private LocalDateTime dob;

    @Column(length = 55)
    private String instagramId;

    @Column(length = 255)
    private String token;

    @Column
    private Boolean isActive;

    //    tambahan
    @OneToMany(mappedBy = "influencer")
    private List<ProjectHeader> projectHeaders;

    @OneToMany(mappedBy = "influencer")
    private List<Review> reviews;

    @OneToMany(mappedBy = "influencer")
    private List<InfluencerMediaType> influencerMediaTypes;

//    many to many
    @ManyToMany
    @JoinTable(
            name = "influencer_category",
            joinColumns = @JoinColumn(name = "influencer_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    //    many to many
    @ManyToMany(mappedBy = "influencers")
    private List<Brand> brands;

}

package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 55)
    private String password;

    @Column(length = 5000000)
    private byte[] profilePictureByte;

    private String profilePictureType;
    private String profilePictureName;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    //    tambahan
    @OneToMany(mappedBy = "brand")
    private List<ProjectHeader> projectHeaders;

    @OneToMany(mappedBy = "brand")
    private List<Review> reviews;

//    many to many
    @ManyToMany
    @JoinTable(
            name = "brand_target_age",
            joinColumns = @JoinColumn(name = "brand_id"),
            inverseJoinColumns = @JoinColumn(name = "age_id")
    )
    private List<Age> ages;

    @ManyToMany
    @JoinTable(
            name = "brand_target_location",
            joinColumns = @JoinColumn(name = "brand_id"),
            inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private List<Location> locations;

    @ManyToMany
    @JoinTable(
            name = "brand_target_gender",
            joinColumns = @JoinColumn(name = "brand_id"),
            inverseJoinColumns = @JoinColumn(name = "gender_id")
    )
    private List<Gender> genders;

    @ManyToMany
    @JoinTable(
            name = "saved_influencer",
            joinColumns = @JoinColumn(name = "brand_id"),
            inverseJoinColumns = @JoinColumn(name = "influencer_id")
    )
    private List<Influencer> influencers;
}

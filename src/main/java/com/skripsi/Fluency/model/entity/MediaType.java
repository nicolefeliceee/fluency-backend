package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table
@Data
public class MediaType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 55)
    private String label;

    //    tambahan
    @OneToMany(mappedBy = "mediaType")
    private List<ProjectDetail> projectDetails;

    @OneToMany(mappedBy = "mediaType")
    private List<InfluencerMediaType> influencerMediaTypes;
}

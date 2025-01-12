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

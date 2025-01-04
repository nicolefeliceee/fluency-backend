package com.skripsi.Fluency.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class InfluencerMediaType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "influencer_id")
    private Influencer influencer;

    @ManyToOne
    @JoinColumn(name = "media_type_id")
    private MediaType mediaType;

    @Column
    private Double price;

}

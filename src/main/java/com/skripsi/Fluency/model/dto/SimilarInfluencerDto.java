package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SimilarInfluencerDto {
    private Integer id;
    private Integer influencerId;
    private String username;
    private List<?> category;
    private String profilepicture;
    private double finalscore;
}

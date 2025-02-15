package com.skripsi.Fluency.model.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateReviewDto {
    private String projectHeaderId;
    private String influencerId;
    private String brandId;
    private Integer rating;
    private String review;
}

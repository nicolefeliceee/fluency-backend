package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TotalRatingDto {
    private int rating;
    private int totalreview;
    private double percentage;
}

package com.skripsi.Fluency.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortFilterDto {
    private SortRequestDto sortRequestDto;
    private InfluencerFilterRequestDto influencerFilterRequestDto;
}

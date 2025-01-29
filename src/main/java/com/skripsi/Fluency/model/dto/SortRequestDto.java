package com.skripsi.Fluency.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortRequestDto {
    private List<String> followers2;
    private List<Integer> media2;
    //    private List<String> engagement;
    private List<Integer> gender2;
    private List<String> age2;
    private List<String> price2;
    private List<String> rating2;
    private List<Integer> location2;
    private List<Integer> genderAudience2;
    private List<String> ageAudience2;
    private List<Integer> sort;
    private List<Integer> categoryChosen2;
}

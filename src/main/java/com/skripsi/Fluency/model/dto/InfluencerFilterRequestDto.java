package com.skripsi.Fluency.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class InfluencerFilterRequestDto {
    private List<String> followers;
    private List<Integer> media;
    private List<Integer> gender;
    private List<String> age;
    private List<String> price;
    private List<String> rating;
    private List<Integer> location;
    private List<Integer> genderAudience;
    private List<String> ageAudience;
}

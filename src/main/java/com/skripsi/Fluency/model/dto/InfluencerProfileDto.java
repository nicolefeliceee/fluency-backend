package com.skripsi.Fluency.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InfluencerProfileDto {
    private String influencerId;
    private String name;
    private String email;
    private String location;
    private HashMap<String, String> locationMap;
    private String phone;
    private String gender;
    private HashMap<String, String> genderMap;
    private String dob;
    private String feedsPrice;
    private String reelsPrice;
    private String storyPrice;
    private List<?> category;
    private String userType;
    private String instagramId;
    private Boolean isActive;
    private String token;
}

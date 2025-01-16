package com.skripsi.Fluency.model.dto;

import lombok.Data;

@Data
public class SignupInfluencerRequestDto {
    private String userId;
    private String name;
    private String email;
    private Integer location;
    private String phone;
    private String gender;
    private String dob;
    private String feedsPrice;
    private String reelsPrice;
    private String storyPrice;
    private String[] category;
    private String userType;
    private String instagramId;
    private Boolean isActive;
    private String token;
}

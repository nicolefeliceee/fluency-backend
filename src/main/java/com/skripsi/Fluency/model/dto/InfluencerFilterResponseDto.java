package com.skripsi.Fluency.model.dto;

import com.skripsi.Fluency.model.entity.Gender;
import com.skripsi.Fluency.model.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class InfluencerFilterResponseDto {

    private Integer id;
    private String name;
    private String email;
    private String location;
    private String phone;
    private String gender;
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

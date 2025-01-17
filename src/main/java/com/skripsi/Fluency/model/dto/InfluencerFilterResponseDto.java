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
    private String feedsprice;
    private String reelsprice;
    private String storyprice;
    private List<?> category;
    private String usertype;
    private String instagramid;
    private Boolean isactive;
    private String token;
    private String followers;
    private Double rating;
    private String totalreview;
    private String minprice;
    private String profilepicture;
}

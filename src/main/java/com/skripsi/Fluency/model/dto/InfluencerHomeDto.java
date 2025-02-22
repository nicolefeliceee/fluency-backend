package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class InfluencerHomeDto {
    private Integer id;
    private Integer influencerid;
    private String name;
    private String email;
    private String location;
    private String phone;
    private String gender;
    private List<?> category;
    private String usertype;
    private String instagramid;
    private Boolean isactive;
    private String token;
    private String followers;
    private Double rating;
    private String totalreview;
    private String profilepicture;
    private String postmedia;
    private Double engagement;
    private String following;
    private String username;
    private String bio;
    private String totalrevenue;
    private GraphDto revenueacc;
    private String waitingproj;
    private String ongoingproj;
    private String completedproj;
    private GraphDto projectpct;
    private GraphDto approvalpct;
}

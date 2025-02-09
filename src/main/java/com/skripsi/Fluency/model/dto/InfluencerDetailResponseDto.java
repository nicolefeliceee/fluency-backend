package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class InfluencerDetailResponseDto {
    private Integer id;
    private Integer influencerId;
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
    private String profilepicture;
    private Boolean issaved;
    private String post;
    private String engagement;
    private String following;
    private String igname;
    private String bio;
    private List<?> similarinfluencer;
    private String avglike;
    private String avgcomment;
    private String avgengagement;
    private String avgshare;
    private List<?> totalrating;
    private List<?> positiveanalytics;
    private List<?> negativeanalytics;
    private List<?> neutralanalytics;
    private Integer totalanalyticspost;
    private List<?> feedback;
    private List<?> follgrowth;
    private List<?> accengaged;
    private List<?> reach;
    private List<?> genderaud;
    private List<?> onlinefollaud;
    private String highestonlinetime;
    private String lowestonlinetime;
    private List<?> topcitiesaud;
    private List<?> agerangeaud;
    private List<?> story;
    private List<?> feeds;
    private List<?> reels;
}

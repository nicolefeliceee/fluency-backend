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
    private String postmedia;
    private Double engagement;
    private String following;
    private String username;
    private String bio;
    private String avglike;
    private String avgcomment;
    private String avgsaved;
    private String avgshare;
    private List<TotalRatingDto> totalrating;
    private List<SimilarInfluencerDto> similarinfluencer;

    private GraphDto analytics;
    private Integer totalanalyticspost;

    private List<ReviewDto> feedback;
    private GraphDto follgrowth;
    private GraphDto reach;
    private GraphDto genderaud;
    private GraphDto onlinefollaud;
    private String highestonlinetime;
    private String lowestonlinetime;
    private GraphDto topcitiesaud;
    private GraphDto agerangeaud;
    private List<MediaDetailDto> media;
    private List<MediaDetailDto> feeds;
    private List<MediaDetailDto> reels;
    private List<StoryDetailDto> story;
}

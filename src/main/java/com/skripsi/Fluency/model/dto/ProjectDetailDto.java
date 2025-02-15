package com.skripsi.Fluency.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectDetailDto {
    private String id;
    private String mediatypeId;
    private String note;
    private String deadlineTime;
    private String deadlineDate;
    private String nominal;
    private String link;
    private String statusId;

//    for perfomance
    private String instagramMediaId;
    private String influencerUsername;
    private LocalDateTime analyticsLastUpdated;
    private String analyticsMediaUrl;
    private String analyticsCaption;
    private String analyticsLikes;
    private String analyticsComments;
    private String analyticsSaved;
    private String analyticsShared;
    private String analyticsAccountsEngaged;
    private String analyticsAccountsReached;
    private Double sentimentPositive;
    private Double sentimentNegative;
    private Double sentimentNeutral;
}
